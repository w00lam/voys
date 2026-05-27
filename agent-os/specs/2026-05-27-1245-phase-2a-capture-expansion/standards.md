# Standards: Phase 2A Capture Expansion

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Keep controllers thin.
- Keep upload validation and memo title rules in application/domain code.
- Store audio through `StoragePort`.
- Do not expose local paths, storage keys, stack traces, or raw command output in API responses.
- Preserve existing browser recording upload validation.

## API

- Follow `agent-os/standards/api/rest.md`.
- Use `/api/memos/audio-files` for imported audio creation.
- Use `PATCH /api/memos/{memoId}` for memo metadata updates.
- Return stable safe errors for invalid imports and invalid title updates.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Keep import and title editing in the existing memo workspace.
- Use `apiPostForm` or equivalent CSRF-aware multipart helper for import.
- Add a CSRF-aware PATCH helper if needed.
- Keep recording, playback, transcription polling, search, and timestamp seek behavior intact.

## Tests

- Backend tests should cover service validation and controller delegation.
- Frontend tests should cover import success, import failure, title edit success, and title validation failure.
- Tests should not invoke real Whisper or require real microphone access.
