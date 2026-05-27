# Shape: Phase 2B Suggested Titles

## Desired Flow

1. User records or imports audio.
2. Transcription completes.
3. Backend derives a suggested title from the transcript text.
4. Backend stores the suggestion separately from the accepted memo title.
5. Memo detail loads transcript data.
6. Transcript response includes `suggestedTitle`.
7. Frontend shows a compact suggested-title action near the title editing area or transcript panel.
8. User can keep the current title, edit manually, or adopt the suggestion.

## Heuristic

Start with a conservative local heuristic:

- Use the first non-empty sentence or line from transcript text.
- Trim whitespace.
- Remove terminal punctuation.
- Collapse repeated spaces.
- Limit to the memo title length limit.
- Return no suggestion when transcript text has no readable content.

Do not introduce an LLM/provider for this slice.

## Safety Rules

- Suggested title must not overwrite `VoiceMemo.title` automatically.
- Manual title editing remains authoritative.
- Re-running transcription may refresh `suggestedTitle`, but it must still remain separate from `title`.
- API responses must not expose raw model/provider details because there is no provider in this slice.

## API Shape

Extend transcript response:

```json
{
  "memoId": "...",
  "status": "COMPLETED",
  "text": "...",
  "suggestedTitle": "Product strategy sync",
  "segments": [],
  "failureReason": null,
  "updatedAt": "..."
}
```

Title adoption should reuse:

```text
PATCH /api/memos/{memoId}
```
