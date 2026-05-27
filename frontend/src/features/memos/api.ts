import { apiGet, apiGetBlob, apiPost, apiPostForm, apiPatch } from '../../shared/api/client';

export type MemoSummary = {
  id: string;
  title: string;
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

export function listMemos(): Promise<MemoSummary[]> {
  return apiGet<MemoSummary[]>('/api/memos');
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
