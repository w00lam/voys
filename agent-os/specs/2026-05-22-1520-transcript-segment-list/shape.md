# Shape: Transcript Segment List

## Current Problem

Transcript segments are stored and search results can jump to a timestamp, but the memo detail transcript view still shows only the full raw transcript text. Users cannot browse or click segment timestamps from the memo itself.

## Desired Flow

1. User opens a memo.
2. Frontend fetches memo detail, audio, and transcript.
3. Transcript response includes the full text and ordered segments.
4. The transcript panel shows each segment with a timestamp and text.
5. User clicks a segment.
6. The audio element seeks to the segment start time.
7. Audio remains paused unless the user presses play.

## API Contract

`GET /api/memos/{memoId}/transcript` returns:

```json
{
  "memoId": "33333333-3333-3333-3333-333333333333",
  "status": "COMPLETED",
  "text": "full transcript text",
  "updatedAt": "2026-05-22T10:01:00Z",
  "segments": [
    {
      "position": 0,
      "startSeconds": 0.0,
      "endSeconds": 4.2,
      "text": "intro"
    }
  ]
}
```

When no transcript exists, `segments` should be an empty list.

## Implementation Direction

- Add a repository method that returns segments ordered by `position`.
- Map segments to DTOs inside the transcription application service.
- Keep ownership validation in `getTranscript`.
- Keep the frontend transcript text visible, but render segments when they are available.
- Reuse the existing audio ref seek behavior.
