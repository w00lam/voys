import { afterEach, describe, expect, test, vi } from 'vitest';

import { importAudioFile, updateMemoTitle } from './api';

describe('memo api', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  test('imports audio files through the audio-files endpoint with multipart form data', async () => {
    const fetchMock = installFetchMock();
    const file = new File(['audio'], 'meeting.mp3', { type: 'audio/mpeg' });

    await importAudioFile(file, 120);

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/audio-files', expect.objectContaining({
      method: 'POST',
      credentials: 'include',
    }));
    const [, init] = fetchMock.mock.calls.find(([input]) => input === '/api/memos/audio-files')!;
    expect(init?.body).toBeInstanceOf(FormData);
    expect((init?.body as FormData).get('audio')).toBe(file);
    expect((init?.body as FormData).get('durationSeconds')).toBe('120');
  });

  test('updates memo titles through the memo metadata endpoint', async () => {
    const fetchMock = installFetchMock();

    await updateMemoTitle('33333333-3333-3333-3333-333333333333', 'Product strategy sync');

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333', expect.objectContaining({
      method: 'PATCH',
      credentials: 'include',
      headers: expect.objectContaining({
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': 'csrf-token',
      }),
      body: JSON.stringify({ title: 'Product strategy sync' }),
    }));
  });
});

function installFetchMock() {
  const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const path = typeof input === 'string' ? input : input.toString();

    if ((init?.method ?? 'GET') === 'GET' && path === '/api/auth/csrf') {
      return jsonResponse({ headerName: 'X-XSRF-TOKEN', token: 'csrf-token' });
    }

    if ((init?.method ?? 'GET') === 'POST' && path === '/api/memos/audio-files') {
      return jsonResponse({
        id: '33333333-3333-3333-3333-333333333333',
        title: 'meeting',
        recordingStatus: 'UPLOADED',
        transcriptionStatus: 'PENDING',
        createdAt: '2026-05-27T12:00:00Z',
      }, 201);
    }

    if ((init?.method ?? 'GET') === 'PATCH' && path === '/api/memos/33333333-3333-3333-3333-333333333333') {
      return jsonResponse({
        id: '33333333-3333-3333-3333-333333333333',
        title: 'Product strategy sync',
      });
    }

    return jsonResponse({ code: 'test.not_found', message: `${init?.method ?? 'GET'} ${path}` }, 404);
  });

  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
