# Standards: Search Timestamp Jump

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Follow `agent-os/standards/api/rest.md`.
- Do not return JPA entities directly.
- Keep search owner filtering mandatory.
- Keep blank query validation unchanged.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Keep the search UI keyboard-accessible.
- Do not auto-play audio after seeking.
- Keep existing memo list and transcript polling behavior working.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Backend tests should cover transcript segment timestamp mapping and title matches with no timestamp.
- Frontend tests should cover timestamp rendering and seeking after selecting a search result.
