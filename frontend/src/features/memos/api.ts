import { apiGet, apiGetBlob, apiPost, apiPostForm, apiPatch, apiGetText } from '../../shared/api/client';

export type MemoSummary = {
  id: string;
  title: string;
  folder: string | null;
  recordingStatus: string;
  transcriptionStatus: string;
  createdAt: string;
  durationSeconds: number | null;
  audioSizeBytes: number;
};

export type MemoDetail = MemoSummary & {
  updatedAt: string;
  audio: {
    contentType: string;
    sizeBytes: number;
    originalFilename: string | null;
    durationSeconds: number | null;
  };
};

export type FailureReason = {
  code: string;
  message: string;
  retryable: boolean;
};

export type TranscriptResponse = {
  memoId: string;
  status: string;
  text: string | null;
  suggestedTitle: string | null;
  updatedAt: string | null;
  segments: TranscriptSegment[];
  failureReason: FailureReason | null;
};

export type TranscriptSegment = {
  position: number;
  startSeconds: number;
  endSeconds: number;
  text: string;
};

export function listMemos(folder?: string): Promise<MemoSummary[]> {
  const url = folder ? `/api/memos?folder=${encodeURIComponent(folder)}` : '/api/memos';
  return apiGet<MemoSummary[]>(url);
}

export function getMemo(id: string): Promise<MemoDetail> {
  return apiGet<MemoDetail>(`/api/memos/${id}`);
}

export function getMemoAudio(id: string): Promise<Blob> {
  return apiGetBlob(`/api/memos/${id}/audio`);
}

export function getTranscript(id: string): Promise<TranscriptResponse> {
  return apiGet<TranscriptResponse>(`/api/memos/${id}/transcript`);
}

export function startTranscription(id: string): Promise<TranscriptResponse> {
  return apiPost<TranscriptResponse, undefined>(`/api/memos/${id}/transcription`);
}

export function retryTranscription(id: string): Promise<TranscriptResponse> {
  return apiPost<TranscriptResponse, undefined>(`/api/memos/${id}/transcription/retry`);
}

export type CreatedMemo = {
  id: string;
  title: string;
  recordingStatus: string;
  transcriptionStatus: string;
  createdAt: string;
};

export function importAudioFile(file: File, durationSeconds?: number): Promise<CreatedMemo> {
  const body = new FormData();
  body.append('audio', file);
  if (durationSeconds !== undefined) {
    body.append('durationSeconds', String(durationSeconds));
  }
  return apiPostForm<CreatedMemo>('/api/memos/audio-files', body);
}

export function updateMemoTitle(id: string, title: string): Promise<{ id: string; title: string }> {
  return apiPatch<{ id: string; title: string }, { title: string }>(`/api/memos/${id}`, { title });
}

export function updateMemoFolder(id: string, folder: string | null): Promise<{ id: string; title: string; folder: string | null }> {
  return apiPatch<{ id: string; title: string; folder: string | null }, { folder: string | null }>(`/api/memos/${id}`, { folder });
}

export type GeneratedNoteResponse = {
  memoId: string;
  status: string;
  summary: string | null;
  keyPoints: string[];
  actionItems: string[];
  failureReason: string | null;
  updatedAt: string | null;
};

export function generateGeneratedNote(memoId: string): Promise<GeneratedNoteResponse> {
  return apiPost<GeneratedNoteResponse, undefined>(`/api/memos/${memoId}/generated-note`);
}

export function getGeneratedNote(memoId: string): Promise<GeneratedNoteResponse> {
  return apiGet<GeneratedNoteResponse>(`/api/memos/${memoId}/generated-note`);
}

export function updateGeneratedNote(
  memoId: string,
  updates: { summary: string; keyPoints: string[]; actionItems: string[] }
): Promise<GeneratedNoteResponse> {
  return apiPatch<GeneratedNoteResponse, { summary: string; keyPoints: string[]; actionItems: string[] }>(
    `/api/memos/${memoId}/generated-note`,
    updates
  );
}

export function exportGeneratedNote(memoId: string): Promise<string> {
  return apiGetText(`/api/memos/${memoId}/generated-note/export`);
}

export function exportTranscript(memoId: string): Promise<string> {
  return apiGetText(`/api/memos/${memoId}/transcript/export`);
}
