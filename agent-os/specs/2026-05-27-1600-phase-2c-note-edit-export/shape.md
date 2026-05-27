# Shape: Phase 2C Note Edit And Export

## User Flow

1. User opens a completed memo.
2. User generates or reads the generated note.
3. User edits summary, key points, or action items in memo detail.
4. User saves the generated note.
5. Backend validates ownership and note content.
6. User downloads/copies a plain text export for the generated note or transcript.

## API Shape

```http
PATCH /api/memos/{memoId}/generated-note
Content-Type: application/json

{
  "summary": "Updated summary",
  "keyPoints": ["Point one", "Point two"],
  "actionItems": ["Follow up"]
}
```

```http
GET /api/memos/{memoId}/generated-note/export
GET /api/memos/{memoId}/transcript/export
```

Export responses should be `text/plain; charset=UTF-8`.

## Frontend Shape

- Keep editing inside the generated note panel.
- Use textarea controls for summary and newline-separated lists.
- Keep buttons compact and predictable.
- Use browser blob download for export responses.
