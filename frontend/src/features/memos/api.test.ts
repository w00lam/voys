import { afterEach, describe, expect, test, vi } from 'vitest';

import {
  exportGeneratedNote,
  exportTranscript,
  generateGeneratedNote,
  getGeneratedNote,
  getTranscript,
  importAudioFile,
  listMemos,
  retryTranscription,
  updateMemoFolder,
  updateGeneratedNote,
  updateMemoTitle,
} from './api';

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

  test('updates memo folders through the memo metadata endpoint', async () => {
    const fetchMock = installFetchMock();

    await updateMemoFolder('33333333-3333-3333-3333-333333333333', 'Work');

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333', expect.objectContaining({
      method: 'PATCH',
      credentials: 'include',
      headers: expect.objectContaining({
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': 'csrf-token',
      }),
      body: JSON.stringify({ folder: 'Work' }),
    }));
  });

  test('filters memo lists by folder', async () => {
    const fetchMock = installFetchMock();

    const memos = await listMemos('Work');

    expect(fetchMock).toHaveBeenCalledWith('/api/memos?folder=Work', { credentials: 'include' });
    expect(memos[0].folder).toBe('Work');
  });

  test('reads suggested title from transcript responses', async () => {
    installFetchMock();

    const transcript = await getTranscript('33333333-3333-3333-3333-333333333333');

    expect(transcript.suggestedTitle).toBe('Product strategy sync');
  });

  test('retries failed transcription through the retry endpoint', async () => {
    const fetchMock = installFetchMock();

    const transcript = await retryTranscription('33333333-3333-3333-3333-333333333333');

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333/transcription/retry', expect.objectContaining({
      method: 'POST',
      credentials: 'include',
    }));
    expect(transcript.status).toBe('PROCESSING');
    expect(transcript.failureReason).toBeNull();
  });

  test('generates memo notes through the generated-note endpoint', async () => {
    const fetchMock = installFetchMock();

    const note = await generateGeneratedNote('33333333-3333-3333-3333-333333333333');

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333/generated-note', expect.objectContaining({
      method: 'POST',
      credentials: 'include',
    }));
    expect(note.summary).toContain('launch strategy');
    expect(note.keyPoints).toContain('Launch risks');
    expect(note.actionItems).toContain('Follow up on owners');
  });

  test('reads memo generated notes', async () => {
    const fetchMock = installFetchMock();

    const note = await getGeneratedNote('33333333-3333-3333-3333-333333333333');

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333/generated-note', { credentials: 'include' });
    expect(note.status).toBe('GENERATED');
  });

  test('updates generated notes through the generated-note endpoint', async () => {
    const fetchMock = installFetchMock();

    await updateGeneratedNote('33333333-3333-3333-3333-333333333333', {
      summary: 'Edited summary',
      keyPoints: ['Edited point'],
      actionItems: ['Edited action'],
    });

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333/generated-note', expect.objectContaining({
      method: 'PATCH',
      credentials: 'include',
      body: JSON.stringify({
        summary: 'Edited summary',
        keyPoints: ['Edited point'],
        actionItems: ['Edited action'],
      }),
    }));
  });

  test('exports generated notes and transcripts as text', async () => {
    const fetchMock = installFetchMock();

    const noteExport = await exportGeneratedNote('33333333-3333-3333-3333-333333333333');
    const transcriptExport = await exportTranscript('33333333-3333-3333-3333-333333333333');

    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333/generated-note/export', { credentials: 'include' });
    expect(fetchMock).toHaveBeenCalledWith('/api/memos/33333333-3333-3333-3333-333333333333/transcript/export', { credentials: 'include' });
    expect(noteExport).toContain('Edited summary');
    expect(transcriptExport).toContain('Product strategy sync');
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

    if ((init?.method ?? 'GET') === 'GET' && path === '/api/memos?folder=Work') {
      return jsonResponse([
        {
          id: '33333333-3333-3333-3333-333333333333',
          title: 'Product strategy sync',
          folder: 'Work',
          recordingStatus: 'UPLOADED',
          transcriptionStatus: 'COMPLETED',
          createdAt: '2026-05-27T12:00:00Z',
          durationSeconds: 120,
          audioSizeBytes: 4096,
        },
      ]);
    }

    if ((init?.method ?? 'GET') === 'PATCH' && path === '/api/memos/33333333-3333-3333-3333-333333333333') {
      const body = JSON.parse(String(init?.body));
      return jsonResponse({
        id: '33333333-3333-3333-3333-333333333333',
        title: body.title ?? 'Product strategy sync',
        folder: body.folder ?? null,
      });
    }

    if ((init?.method ?? 'GET') === 'GET' && path === '/api/memos/33333333-3333-3333-3333-333333333333/transcript') {
      return jsonResponse({
        memoId: '33333333-3333-3333-3333-333333333333',
        status: 'COMPLETED',
        text: 'Product strategy sync. The team discussed launch risks.',
        suggestedTitle: 'Product strategy sync',
        segments: [],
        failureReason: null,
        updatedAt: '2026-05-27T13:15:00Z',
      });
    }

    if ((init?.method ?? 'GET') === 'POST' && path === '/api/memos/33333333-3333-3333-3333-333333333333/transcription/retry') {
      return jsonResponse({
        memoId: '33333333-3333-3333-3333-333333333333',
        status: 'PROCESSING',
        text: null,
        suggestedTitle: null,
        segments: [],
        failureReason: null,
        updatedAt: null,
      });
    }

    if ((init?.method ?? 'GET') === 'POST' && path === '/api/memos/33333333-3333-3333-3333-333333333333/generated-note') {
      return jsonResponse(generatedNote());
    }

    if ((init?.method ?? 'GET') === 'PATCH' && path === '/api/memos/33333333-3333-3333-3333-333333333333/generated-note') {
      return jsonResponse({
        ...generatedNote(),
        ...JSON.parse(String(init?.body)),
      });
    }

    if ((init?.method ?? 'GET') === 'GET' && path === '/api/memos/33333333-3333-3333-3333-333333333333/generated-note') {
      return jsonResponse(generatedNote());
    }

    if ((init?.method ?? 'GET') === 'GET' && path === '/api/memos/33333333-3333-3333-3333-333333333333/generated-note/export') {
      return textResponse('Summary\nEdited summary');
    }

    if ((init?.method ?? 'GET') === 'GET' && path === '/api/memos/33333333-3333-3333-3333-333333333333/transcript/export') {
      return textResponse('Product strategy sync. The team discussed launch risks.');
    }

    return jsonResponse({ code: 'test.not_found', message: `${init?.method ?? 'GET'} ${path}` }, 404);
  });

  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

function textResponse(body: string, status = 200) {
  return new Response(body, {
    status,
    headers: {
      'Content-Type': 'text/plain; charset=UTF-8',
    },
  });
}

function generatedNote() {
  return {
    memoId: '33333333-3333-3333-3333-333333333333',
    status: 'GENERATED',
    summary: 'The team reviewed launch strategy.',
    keyPoints: ['Launch risks'],
    actionItems: ['Follow up on owners'],
    failureReason: null,
    updatedAt: '2026-05-27T15:30:00Z',
  };
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
