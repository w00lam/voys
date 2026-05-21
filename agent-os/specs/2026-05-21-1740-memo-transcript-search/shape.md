# Shape: Memo And Transcript Search

## Current Problem

Users can record, transcribe, and view transcripts, but they cannot search across saved memo titles or transcript text.

## Desired API

```text
GET /api/search?q={query}
```

The endpoint should:

- require an authenticated session,
- use the authenticated user's id for ownership filtering,
- trim and normalize the query,
- reject blank queries,
- return an array of results.

## Result Shape

Each result should include:

- `memoId`
- `title`
- `matchType`: `TITLE` or `TRANSCRIPT`
- `snippet`
- `transcriptionStatus`

## Matching Rules

- Title matches come from `voice_memos.title`.
- Transcript matches come from `transcripts.text`.
- Search is case-insensitive for the MVP.
- Results must not include another user's memo or transcript.
- Use a small default limit, currently 20 results.

## Implementation Direction

- Add a `search` package following existing backend standards:
  - `search.api`
  - `search.application`
  - `search.domain`
  - `search.infrastructure`
- Keep controllers thin.
- Put query normalization in the application service.
- Put persistence-specific matching in the repository/adapter.
- Keep snippets simple for MVP; returning the matching title or a short transcript excerpt is enough.
