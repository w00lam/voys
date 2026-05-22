# Standards: Transcript Segment List

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Follow `agent-os/standards/api/rest.md`.
- Do not return JPA entities directly.
- Preserve owner-scoped memo lookup before returning transcript data.
- Return segments in stable `position` order.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Keep transcript segments keyboard-accessible.
- Render timestamps in `mm:ss` format.
- Do not auto-play audio after seeking.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Backend tests should verify ordered segment mapping.
- Frontend tests should verify rendering and click-to-seek behavior.
