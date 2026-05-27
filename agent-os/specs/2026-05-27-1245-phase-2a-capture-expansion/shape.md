# Shape: Phase 2A Capture Expansion

## Desired Import Flow

1. User signs in.
2. User opens the memo library.
3. User chooses an audio file.
4. Frontend posts the file to an import endpoint with credentials and CSRF protection.
5. Backend validates owner, file presence, content type, size, and optional duration.
6. Backend rejects unsupported or invalid files before storage.
7. Backend stores the audio through `StoragePort`.
8. Backend creates a user-owned memo with an imported source type or equivalent metadata.
9. Backend derives the initial title from the filename when safe.
10. Frontend refreshes the memo list and lets the user select the imported memo.

## Desired Title Flow

1. User opens a memo they own.
2. User edits the title near the memo heading.
3. Frontend sends a metadata update request.
4. Backend validates ownership and title constraints.
5. Backend persists the title only.
6. Frontend updates memo detail and memo list state.

## API Direction

Proposed endpoints:

```text
POST  /api/memos/audio-files
PATCH /api/memos/{memoId}
```

`POST /api/memos/audio-files` should use multipart form data.
The audio part should be named `audio`, matching the existing recording upload.

`PATCH /api/memos/{memoId}` should accept explicit metadata fields.
For Phase 2A, only title updates are in scope.

## Supported Audio Policy

Start with:

- `audio/webm`
- `audio/mpeg`
- `audio/wav`

The implementation may support additional safe formats if the validation remains explicit and tests stay focused.

## Title Rules

- Trim surrounding whitespace.
- Reject blank titles.
- Reject titles longer than 200 characters.
- For imported filenames, remove the final extension and normalize separators into readable spaces.
- Fall back to the existing temporary title generator when no readable filename title remains.

## UX Direction

- Add import inside the authenticated memo workspace.
- Keep the current browser recorder.
- Keep imported files and browser recordings in one memo list.
- Add title editing near the selected memo title.
- Use concise Korean UI copy consistent with the existing app.
