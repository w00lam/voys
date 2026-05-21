import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';

import App from './App';

const memoId = '33333333-3333-3333-3333-333333333333';

describe('App transcription polling', () => {
  beforeEach(() => {
    URL.createObjectURL = vi.fn(() => 'blob:audio');
    URL.revokeObjectURL = vi.fn();
  });

  afterEach(() => {
    cleanup();
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  test('polls transcript status after starting transcription and shows completed text', async () => {
    const fetchMock = mockApi();
    vi.stubGlobal('fetch', fetchMock);

    render(<App />);

    fireEvent.click(await screen.findByRole('button', { name: /Lecture about product strategy/i }));
    await screen.findByText('No transcript has been created yet.');

    vi.useFakeTimers();
    fireEvent.click(screen.getByRole('button', { name: /Start transcription/i }));

    await act(async () => {});
    expect(screen.getAllByText(/processing/i).length).toBeGreaterThan(0);

    await act(async () => {
      await vi.advanceTimersByTimeAsync(6_000);
    });

    expect(screen.getByText('Final transcript text from the background worker.')).toBeInTheDocument();
    expect(screen.getAllByText(/completed/i).length).toBeGreaterThan(0);
    expect(transcriptFetchCount(fetchMock)).toBeGreaterThanOrEqual(3);
  });

  test('renders memo metadata separator without mojibake', async () => {
    vi.stubGlobal('fetch', mockApi());

    render(<App />);

    await screen.findByRole('button', { name: /Lecture about product strategy/i });

    expect(screen.queryByText(/쨌/)).not.toBeInTheDocument();
    expect(screen.getByText((_, element) => element?.textContent === 'pending · 5 B')).toBeInTheDocument();
  });
});

function mockApi() {
  let transcriptReads = 0;
  let memoStatus = 'PENDING';

  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const path = typeof input === 'string' ? input : input.toString();
    const method = init?.method ?? 'GET';

    if (method === 'GET' && path === '/api/me') {
      return jsonResponse({
        id: '11111111-1111-1111-1111-111111111111',
        email: 'user@example.com',
        displayName: 'Voys User',
      });
    }

    if (method === 'GET' && path === '/api/memos') {
      return jsonResponse([
        {
          id: memoId,
          title: 'Lecture about product strategy',
          recordingStatus: 'UPLOADED',
          transcriptionStatus: memoStatus,
          createdAt: '2026-05-21T10:00:00Z',
          durationSeconds: 120,
          audioSizeBytes: 5,
        },
      ]);
    }

    if (method === 'GET' && path === `/api/memos/${memoId}`) {
      return jsonResponse({
        id: memoId,
        title: 'Lecture about product strategy',
        recordingStatus: 'UPLOADED',
        transcriptionStatus: memoStatus,
        createdAt: '2026-05-21T10:00:00Z',
        updatedAt: '2026-05-21T10:00:00Z',
        durationSeconds: 120,
        audioSizeBytes: 5,
        audio: {
          contentType: 'audio/webm;codecs=opus',
          sizeBytes: 5,
          originalFilename: 'lecture.webm',
          durationSeconds: 120,
        },
      });
    }

    if (method === 'GET' && path === `/api/memos/${memoId}/audio`) {
      return new Response(new Blob(['audio'], { type: 'audio/webm' }), { status: 200 });
    }

    if (method === 'GET' && path === '/api/auth/csrf') {
      return jsonResponse({ headerName: 'X-XSRF-TOKEN', token: 'csrf-token' });
    }

    if (method === 'POST' && path === `/api/memos/${memoId}/transcription`) {
      memoStatus = 'PROCESSING';
      return jsonResponse({
        memoId,
        status: 'PROCESSING',
        text: null,
        updatedAt: null,
      });
    }

    if (method === 'GET' && path === `/api/memos/${memoId}/transcript`) {
      transcriptReads += 1;
      if (transcriptReads >= 3) {
        memoStatus = 'COMPLETED';
        return jsonResponse({
          memoId,
          status: 'COMPLETED',
          text: 'Final transcript text from the background worker.',
          updatedAt: '2026-05-21T10:01:00Z',
        });
      }

      return jsonResponse({
        memoId,
        status: memoStatus,
        text: null,
        updatedAt: null,
      });
    }

    return jsonResponse({ code: 'test.not_found', message: `${method} ${path}` }, 404);
  });
}

function transcriptFetchCount(fetchMock: ReturnType<typeof vi.fn>) {
  return fetchMock.mock.calls.filter(([input, init]) => {
    const path = typeof input === 'string' ? input : input.toString();
    const method = init?.method ?? 'GET';
    return method === 'GET' && path === `/api/memos/${memoId}/transcript`;
  }).length;
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
