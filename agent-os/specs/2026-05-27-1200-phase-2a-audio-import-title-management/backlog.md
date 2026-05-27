# Backlog: Phase 2A Audio Import And Title Management

## Implementation Epics

### 1. Audio Import API

- Define accepted content types and file size limits.
- Add authenticated `POST /api/memos/audio-files`.
- Validate owner, file presence, content type, size, and duration when available.
- Store imported audio through the existing storage port.
- Create a memo with source metadata and initial title.
- Start or enqueue transcription using the existing workflow.
- Return safe error envelopes for validation failures.

### 2. Memo Title Editing API

- Add title validation rules.
- Add authenticated `PATCH /api/memos/{memoId}` for title updates.
- Enforce user ownership.
- Update memo list/detail response shapes if needed.
- Ensure title search uses the edited title.

### 3. Frontend Import UX

- Add an import action to the memo library.
- Add file picker flow with supported format copy.
- Show upload progress and upload failure states.
- Insert or refresh the created memo after upload.
- Reuse memo detail transcription status UI.

### 4. Frontend Title Editing UX

- Add inline or focused title edit control in memo detail.
- Keep keyboard and screen-reader behavior clear.
- Show validation and save failures without losing the current title.
- Reflect saved title in memo list and search results.

### 5. Tests And Verification

- Backend API tests for authenticated upload, unsupported type, empty file, too-large file, ownership isolation, and title validation.
- Backend service tests for imported memo creation and title update behavior.
- Frontend tests for upload success, upload failure, title edit success, and title validation.
- Manual checks for browser recording regression, imported playback, transcription status polling, completed transcript search, and timestamp seek.

## Suggested PR Breakdown

1. Backend audio import contract and tests.
2. Backend title editing contract and tests.
3. Frontend import UI and tests.
4. Frontend title editing UI and tests.
5. Manual verification and documentation polish.

## Open Questions

- What exact upload size limit matches the current 2-hour recording policy across supported formats?
- Should imported audio start transcription automatically, matching browser recording behavior, or wait for an explicit start action?
- Should filename-derived titles preserve extensions in any case, or always remove them?
- Which MIME types should be accepted in the first implementation after verifying local playback and Whisper compatibility?
- Is duration validation required before storage, or can it happen after storage with a cleanup path for invalid uploads?
