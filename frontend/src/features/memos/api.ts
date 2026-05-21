import { apiGet, apiGetBlob } from '../../shared/api/client';

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

export function listMemos(): Promise<MemoSummary[]> {
  return apiGet<MemoSummary[]>('/api/memos');
}

export function getMemo(id: string): Promise<MemoDetail> {
  return apiGet<MemoDetail>(`/api/memos/${id}`);
}

export function getMemoAudio(id: string): Promise<Blob> {
  return apiGetBlob(`/api/memos/${id}/audio`);
}
