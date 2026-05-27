# Standards: Phase 2A Audio Import And Title Management

## Product

- Align with `agent-os/product/mission.md`: recordings should become searchable and easier to revisit.
- Align with `agent-os/product/roadmap.md` Phase 2: audio file upload and editable titles are post-launch capabilities.
- Keep transcript editing, summaries, generated documents, and integrations out of Phase 2A.

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Keep upload and title update logic in application services, not controllers.
- Store raw audio on the filesystem through the existing storage boundary.
- Do not store raw audio bytes in PostgreSQL.
- Do not run long transcription work inside the upload request transaction.
- Enforce user ownership for upload-created memos and title updates.
- Keep API errors safe; do not expose local paths, stack traces, command output, or storage keys.

## API

- Follow `agent-os/standards/api/rest.md`.
- Prefix endpoints with `/api`.
- Use `POST /api/memos/audio-files` for imported audio memo creation.
- Use `PATCH /api/memos/{memoId}` for memo metadata updates.
- Return stable error codes and user-safe messages.
- Use explicit request and response DTOs.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Keep upload and title editing in the existing memo feature area.
- Include credentials for authenticated API calls.
- Keep recording, imported playback, transcription status polling, transcript display, and search behavior consistent.
- Do not store session tokens in browser-accessible storage.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Add backend tests for validation, ownership, storage coordination, and title updates.
- Add frontend tests for import and title edit states.
- Avoid real Whisper runs in normal automated tests.
- Manually verify existing browser recording still works after import support is added.
