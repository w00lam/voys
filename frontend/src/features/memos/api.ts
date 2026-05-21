import { apiGet, apiGetBlob, apiPost } from '../../shared/api/client';

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

export type TranscriptResponse = {
  memoId: string;
  status: string;
  text: string | null;
  updatedAt: string | null;
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
