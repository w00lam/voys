# Standards: Recording Duration Limit

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Follow `agent-os/standards/api/rest.md`.
- Keep upload validation in the application service.
- Preserve existing file size and content type checks.
- Return safe user-facing validation messages.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Use MediaRecorder state transitions explicitly.
- Keep recording controls keyboard accessible.
- Do not depend on real microphone access in tests.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Backend tests should use fake ports and mocks, not real file storage.
- Frontend tests should mock MediaRecorder and browser media APIs.
