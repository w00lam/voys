# Shape: Phase 2B Library Folders

## User Flow

1. User imports or records audio.
2. The memo appears in the existing memo library.
3. User opens the memo detail.
4. User enters a folder name such as `Work` or `Study`.
5. Frontend sends a memo metadata update request with `folder`.
6. Backend validates ownership and folder constraints.
7. Backend persists the folder on the memo.
8. Frontend refreshes memo list/detail state.
9. User filters the library by folder to focus repeated-use review.

## API Shape

Keep folder metadata inside the existing memo metadata boundary.

```http
PATCH /api/memos/{memoId}
Content-Type: application/json

{
  "folder": "Work"
}
```

The same endpoint may continue to support title updates.
The response should include the memo id, title, and folder after the update.

Memo summary and detail responses should include:

```json
{
  "folder": "Work"
}
```

List filtering should use a simple query parameter:

```http
GET /api/memos?folder=Work
```

## Validation

- `null`, missing, or blank folder clears the folder.
- Non-blank folders are trimmed.
- Folder names must not exceed 80 characters.
- Folder updates must use owner-scoped memo lookup.

## Frontend Shape

- Keep folder controls inside the selected memo detail area near title metadata.
- Add a compact folder filter in the memo library area.
- Use the same CSRF-aware JSON client used for title updates.
- Avoid adding a separate organization page.
