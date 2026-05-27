# Shape: Phase 2A Audio Import And Title Management

## Current Problem

The MVP is useful when the user records directly in Chrome or Edge, but many real meetings, lectures, interviews, and study sessions are captured somewhere else first.
Those recordings cannot be brought into Voys yet, so the searchable memo library is limited to browser-recorded audio.

The MVP also creates automatic temporary titles.
That is good for quick capture, but repeated use requires recognizable names, especially when imported audio files already have meaningful filenames.

## Desired Upload Flow

1. User signs in.
2. User opens the memo library.
3. User chooses an audio file to upload.
4. Frontend validates obvious client-side constraints before upload when possible.
5. Frontend sends the file to the backend with credentials.
6. Backend validates authentication, content type, size, and workflow constraints.
7. Backend stores the audio through the storage adapter.
8. Backend creates a user-owned memo with an initial title.
9. Backend starts or enqueues transcription using the same workflow as browser-recorded memos.
10. Frontend shows the memo in the library and shows transcription status in the memo detail view.
11. Existing polling, transcript display, search, timestamp jump, and playback behavior apply.

## Desired Title Flow

1. User opens a memo they own.
2. User edits the memo title.
3. Frontend submits the new title.
4. Backend validates ownership, non-empty title, and maximum length.
5. Backend updates only the memo title metadata.
6. Memo list, memo detail, and search title matches reflect the new title.

## Initial Title Rules

- For browser recordings, keep the existing automatic temporary title behavior.
- For imported audio, prefer a sanitized filename-derived title when available.
- If the filename cannot produce a usable title, fall back to the automatic temporary title format.
- The user can rename either kind of memo.

## Supported File Policy

Start narrow and explicit.
The implementation PR should confirm the exact supported list against browser playback and backend validation, but the design preference is:

- `audio/webm`
- `audio/ogg`
- `audio/mpeg`
- `audio/mp4` or `audio/x-m4a` only if local playback and Whisper handling are validated
- `audio/wav`

Reject unknown, empty, or misleading files with safe errors.
Do not trust the browser-provided content type alone when backend validation can inspect file metadata.

## API Shape

Proposed endpoints:

```text
POST  /api/memos/audio-files
PATCH /api/memos/{memoId}
```

`POST /api/memos/audio-files` creates a memo from an uploaded file.
It should return the created memo summary or detail response with transcription status.

`PATCH /api/memos/{memoId}` updates memo metadata.
For Phase 2A, only title changes are in scope.

## Data Shape

Likely additions or confirmations:

- Memo source type: browser recording or imported file.
- Original filename for imported audio when safe to store.
- Content type and size metadata for imported audio.
- Duration when available, validated against the existing maximum.
- Editable title with validation.

Avoid storing raw audio bytes in PostgreSQL.
Keep the filesystem storage boundary intact.

## UX Notes

- Add import as a first-class action in the memo library, not as a separate product area.
- Keep upload progress visible when available.
- Show safe error text for unsupported type, too-large file, empty file, invalid duration, and network failure.
- Do not make transcription feel like a separate workflow for imported files.
- Keep title editing lightweight and close to the memo title.

## Compatibility Notes

Audio import is not the same as Safari in-browser recording support.
It should help Safari users bring in files, but it should not require solving Safari MediaRecorder behavior in Phase 2A.
