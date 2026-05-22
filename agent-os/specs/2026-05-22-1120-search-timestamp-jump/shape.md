# Shape: Search Timestamp Jump

## Current Problem

Search results can open a memo, but they do not carry the matching transcript timestamp. For long meetings or lectures, opening the memo still leaves the user searching manually.

## Desired Flow

1. User searches for a phrase.
2. Backend returns matching memo results.
3. Transcript matches based on `transcript_segments.text` include `segmentStartSeconds`.
4. Frontend renders the timestamp next to transcript search results.
5. User clicks a timestamped result.
6. The memo, audio, and transcript load.
7. The audio element seeks to `segmentStartSeconds`.
8. Audio stays paused unless the user presses play.

## API Contract

`GET /api/search?q={query}` returns:

```json
[
  {
    "memoId": "33333333-3333-3333-3333-333333333333",
    "title": "Strategy review",
    "matchType": "TRANSCRIPT",
    "snippet": "strategy and launch risks",
    "transcriptionStatus": "COMPLETED",
    "segmentStartSeconds": 42.5
  }
]
```

Title matches return `segmentStartSeconds: null`.

## Implementation Direction

- Prefer segment text as the transcript-match snippet when a matching segment exists.
- Keep completed transcript ownership filtering intact.
- If a transcript has no segments, it may still return a transcript match with `segmentStartSeconds: null`.
- The frontend should pass the selected search result timestamp into memo selection rather than adding another route.
- Use an audio ref or a small post-load effect to set `currentTime` after the audio element is rendered.
