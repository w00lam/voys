import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';

import App from './App';

const memoId = '33333333-3333-3333-3333-333333333333';
const maxRecordingDurationMs = 7_200_000;

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
    await screen.findByText('아직 생성된 전사 내용이 없습니다.');

    vi.useFakeTimers();
    fireEvent.click(screen.getByRole('button', { name: /전사 시작/i }));

    await act(async () => {});
    expect(screen.getAllByText(/처리 중/i).length).toBeGreaterThan(0);

    await act(async () => {
      await vi.advanceTimersByTimeAsync(6_000);
    });

    expect(screen.getByText('Final transcript text from the background worker.')).toBeInTheDocument();
    expect(screen.getAllByText(/완료/i).length).toBeGreaterThan(0);
    expect(transcriptFetchCount(fetchMock)).toBeGreaterThanOrEqual(3);
  });

  test('shows a safe transcription failure reason when transcription fails', async () => {
    vi.stubGlobal('fetch', mockApi({ failTranscriptionStart: true }));

    render(<App />);

    fireEvent.click(await screen.findByRole('button', { name: /Lecture about product strategy/i }));
    await screen.findByText('아직 생성된 전사 내용이 없습니다.');

    fireEvent.click(screen.getByRole('button', { name: '전사 시작' }));

    expect(await screen.findByText('Whisper CLI is not installed or not available to the backend process.')).toBeInTheDocument();
    expect(document.querySelector('audio')).toBeInTheDocument();
  });

  test('explains that long or first transcriptions can take several minutes while processing', async () => {
    vi.stubGlobal('fetch', mockApi({ initialStatus: 'PROCESSING' }));

    render(<App />);

    fireEvent.click(await screen.findByRole('button', { name: /Lecture about product strategy/i }));

    expect(await screen.findByText('긴 녹음이나 첫 전사는 몇 분 이상 걸릴 수 있습니다.')).toBeInTheDocument();
    expect(document.querySelector('audio')).toBeInTheDocument();
  });

  test('renders memo metadata separator without mojibake', async () => {
    vi.stubGlobal('fetch', mockApi());

    render(<App />);

    await screen.findByRole('button', { name: /Lecture about product strategy/i });

    expect(screen.queryByText(/쨌|夷|�/)).not.toBeInTheDocument();
    expect(screen.getByText((_, element) => element?.textContent === '대기 중 · 5 B')).toBeInTheDocument();
  });

  test('searches memo titles and transcript snippets, then opens a selected timestamp result', async () => {
    const fetchMock = mockApi();
    vi.stubGlobal('fetch', fetchMock);

    render(<App />);

    const searchInput = await screen.findByRole('textbox', { name: /녹음 검색/i });

    vi.useFakeTimers();
    fireEvent.change(searchInput, { target: { value: 'strategy' } });

    await act(async () => {
      await vi.advanceTimersByTimeAsync(500);
    });

    expect(fetchMock).toHaveBeenCalledWith('/api/search?q=strategy', { credentials: 'include' });
    expect(screen.getByText('Strategy review')).toBeInTheDocument();
    expect(screen.getByText('전사')).toBeInTheDocument();
    expect(screen.getByText('The team discussed strategy and launch risks.')).toBeInTheDocument();
    expect(screen.getByText('00:42')).toBeInTheDocument();

    vi.useRealTimers();
    fireEvent.click(screen.getByRole('button', { name: /Strategy review/i }));

    expect(await screen.findByText('선택한 메모')).toBeInTheDocument();
    expect(screen.getByText('Strategy review')).toBeInTheDocument();
    expect(screen.getByText('Transcript text for strategy review.')).toBeInTheDocument();

    await waitFor(() => {
      const audio = document.querySelector('audio');
      expect(audio?.currentTime).toBe(42.5);
      expect(audio?.paused).toBe(true);
    });
  });

  test('renders transcript segments and seeks audio when a segment is clicked', async () => {
    vi.stubGlobal('fetch', mockApi());

    render(<App />);

    const searchInput = await screen.findByRole('textbox', { name: /녹음 검색/i });

    vi.useFakeTimers();
    fireEvent.change(searchInput, { target: { value: 'strategy' } });

    await act(async () => {
      await vi.advanceTimersByTimeAsync(500);
    });

    vi.useRealTimers();
    fireEvent.click(screen.getByRole('button', { name: /Strategy review/i }));

    expect(await screen.findByRole('button', { name: /00:42 strategy and launch risks/i })).toBeInTheDocument();

    const audio = document.querySelector('audio');
    expect(audio).not.toBeNull();
    audio!.currentTime = 0;

    fireEvent.click(screen.getByRole('button', { name: /00:42 strategy and launch risks/i }));

    expect(audio?.currentTime).toBe(42.5);
    expect(audio?.paused).toBe(true);
  });

  test('automatically stops browser recording at the two hour limit', async () => {
    const fetchMock = mockApi();
    vi.stubGlobal('fetch', fetchMock);

    const recorder = installMediaRecorderMock();

    render(<App />);

    const startButton = await screen.findByRole('button', { name: /녹음 시작/i });
    expect(screen.getByText(/최대 2시간/i)).toBeInTheDocument();

    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-05-22T10:00:00Z'));
    fireEvent.click(startButton);

    await act(async () => {});

    await act(async () => {
      await vi.advanceTimersByTimeAsync(maxRecordingDurationMs);
    });

    expect(recorder.stop).toHaveBeenCalledTimes(1);

    vi.useRealTimers();

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/memos/recordings',
        expect.objectContaining({ method: 'POST' })
      );
    });
  });
});

function installMediaRecorderMock() {
  const stream = {
    getTracks: () => [{ stop: vi.fn() }],
  };
  vi.stubGlobal('navigator', {
    mediaDevices: {
      getUserMedia: vi.fn(async () => stream),
    },
  });

  const recorder = {
    start: vi.fn(),
    stop: vi.fn(),
    addEventListener: vi.fn(),
  };

  const listeners = new Map<string, Array<(event?: unknown) => void>>();
  recorder.addEventListener.mockImplementation((eventName: string, listener: (event?: unknown) => void) => {
    listeners.set(eventName, [...(listeners.get(eventName) ?? []), listener]);
  });
  recorder.stop.mockImplementation(() => {
    for (const listener of listeners.get('dataavailable') ?? []) {
      listener({ data: new Blob(['audio'], { type: 'audio/webm' }) });
    }
    for (const listener of listeners.get('stop') ?? []) {
      listener();
    }
  });

  const MediaRecorderMock = vi.fn(function() { return recorder; });
  Object.assign(MediaRecorderMock, {
    isTypeSupported: vi.fn(() => true),
  });
  vi.stubGlobal('MediaRecorder', MediaRecorderMock);

  return recorder;
}

function mockApi(options: { failTranscriptionStart?: boolean; initialStatus?: string } = {}) {
  let transcriptReads = 0;
  let memoStatus = options.initialStatus ?? 'PENDING';
  let selectedTitle = 'Lecture about product strategy';

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
          title: selectedTitle,
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
        title: selectedTitle,
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
      if (options.failTranscriptionStart) {
        memoStatus = 'FAILED';
        return jsonResponse({
          memoId,
          status: 'FAILED',
          text: null,
          segments: [],
          failureReason: {
            code: 'WHISPER_COMMAND_NOT_FOUND',
            message: 'Whisper CLI is not installed or not available to the backend process.',
            retryable: true,
          },
          updatedAt: '2026-05-24T09:45:00Z',
        });
      }

      memoStatus = 'PROCESSING';
      return jsonResponse({
        memoId,
        status: 'PROCESSING',
        text: null,
        segments: [],
        failureReason: null,
        updatedAt: null,
      });
    }

    if (method === 'POST' && path === '/api/memos/recordings') {
      return jsonResponse({
        id: memoId,
        title: 'Recording 2026-05-22 19:00',
        recordingStatus: 'UPLOADED',
        transcriptionStatus: 'PENDING',
        createdAt: '2026-05-22T10:00:00Z',
      }, 201);
    }

    if (method === 'GET' && path === `/api/memos/${memoId}/transcript`) {
      transcriptReads += 1;
      if (transcriptReads >= 3 && selectedTitle !== 'Strategy review') {
        memoStatus = 'COMPLETED';
        return jsonResponse({
          memoId,
          status: 'COMPLETED',
          text: 'Final transcript text from the background worker.',
          segments: [],
          failureReason: null,
          updatedAt: '2026-05-21T10:01:00Z',
        });
      }

      return jsonResponse({
        memoId,
        status: memoStatus,
        text: selectedTitle === 'Strategy review' ? 'Transcript text for strategy review.' : null,
        segments: selectedTitle === 'Strategy review' ? [
          {
            position: 0,
            startSeconds: 42.5,
            endSeconds: 48.0,
            text: 'strategy and launch risks',
          },
        ] : [
          {
            position: 0,
            startSeconds: 0.0,
            endSeconds: 4.2,
            text: 'intro',
          },
          {
            position: 1,
            startSeconds: 4.2,
            endSeconds: 8.0,
            text: 'roadmap and risks',
          },
        ],
        failureReason: null,
        updatedAt: null,
      });
    }

    if (method === 'GET' && path === '/api/search?q=strategy') {
      selectedTitle = 'Strategy review';
      memoStatus = 'COMPLETED';
      return jsonResponse([
        {
          memoId,
          title: 'Strategy review',
          matchType: 'TRANSCRIPT',
          snippet: 'The team discussed strategy and launch risks.',
          transcriptionStatus: 'COMPLETED',
          segmentStartSeconds: 42.5,
        },
      ]);
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
