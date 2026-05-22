import { apiGet } from '../../shared/api/client';

export type SearchResult = {
  memoId: string;
  title: string;
  matchType: 'TITLE' | 'TRANSCRIPT';
  snippet: string;
  transcriptionStatus: string;
  segmentStartSeconds: number | null;
};

export function searchMemos(query: string): Promise<SearchResult[]> {
  return apiGet<SearchResult[]>(`/api/search?q=${encodeURIComponent(query)}`);
}
