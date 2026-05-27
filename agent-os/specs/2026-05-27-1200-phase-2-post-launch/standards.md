# Standards: Phase 2 Post-Launch

## Product

- Align with `agent-os/product/mission.md`: recordings should become searchable, reviewable, and easier to organize.
- Keep Phase 2 centered on the memo workspace, not disconnected tools.
- Sequence work from capture and organization foundations toward generated notes and integrations.

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Keep user-owned resources protected by ownership checks.
- Keep upload, organization, generated-note, and integration logic in application services, not controllers.
- Keep raw audio on the filesystem through the storage boundary.
- Keep generated-note provider details behind adapter boundaries.
- Do not expose local paths, stack traces, command output, secrets, provider payloads, or storage keys in API responses.

## API

- Follow `agent-os/standards/api/rest.md`.
- Prefix endpoints with `/api`.
- Use explicit request and response DTOs.
- Use stable error codes and user-safe messages.
- Keep long-running work asynchronous and status-based.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Keep new flows inside the existing memo workspace unless a later spec justifies a separate area.
- Include credentials for authenticated API calls.
- Preserve recording, playback, transcription status, transcript, search, and timestamp navigation behavior.
- Keep status changes visible in text, not color alone.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Each implementation slice should add failing tests before production code.
- Avoid real Whisper runs or real generation provider calls in normal automated tests.
- Manually verify existing MVP flows after each slice.
