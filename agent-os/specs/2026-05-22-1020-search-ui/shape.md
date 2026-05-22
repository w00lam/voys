# Shape: Search UI

## Current Problem

The backend exposes `GET /api/search?q={query}`, but the frontend has no search box or result list. Users still need to browse manually.

## Desired Flow

1. Authenticated user sees a search input near the saved memo library.
2. User types a query.
3. The frontend debounces the input.
4. The frontend calls `GET /api/search?q={query}`.
5. Results show:
   - memo title,
   - match type,
   - snippet,
   - transcription status.
6. User clicks a result.
7. The selected memo opens using the existing memo detail/audio/transcript flow.

## API Contract

Use the existing backend endpoint:

```text
GET /api/search?q={query}
```

Expected result item:

```json
{
  "memoId": "33333333-3333-3333-3333-333333333333",
  "title": "Strategy review",
  "matchType": "TRANSCRIPT",
  "snippet": "The team discussed strategy and launch risks.",
  "transcriptionStatus": "COMPLETED"
}
```

## Implementation Direction

- Add frontend API types and a `searchMemos` function near the memo feature API.
- Keep state local in `App.tsx` unless extracting a small component makes the code clearer.
- Use a short debounce interval. Tests advance fake timers by 500ms.
- Reuse the existing `selectMemo` behavior when opening a result.
- Keep search available only for authenticated users.
