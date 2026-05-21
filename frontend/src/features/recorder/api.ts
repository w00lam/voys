import { apiPostForm } from '../../shared/api/client';

export type CreatedMemo = {
  id: string;
  title: string;
  recordingStatus: string;
  transcriptionStatus: string;
  createdAt: string;
};

export function uploadRecording(audio: Blob, durationSeconds: number): Promise<CreatedMemo> {
  const body = new FormData();
  body.append('audio', audio, `recording-${Date.now()}.webm`);
  body.append('durationSeconds', String(durationSeconds));

  return apiPostForm<CreatedMemo>('/api/memos/recordings', body);
}
