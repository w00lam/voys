# Standards for Memo Library Playback

## backend/spring

Use `agent-os/standards/backend/spring.md`.

- Authorize by owner.
- Keep filesystem access behind `StoragePort`.
- Do not expose JPA entities or storage paths in API responses.

## api/rest

Use `agent-os/standards/api/rest.md`.

- Use `/api/memos` resource endpoints.
- Return `404` for missing or non-owned memo resources.
- Keep response DTOs explicit.

## frontend/react

Use `agent-os/standards/frontend/react.md`.

- Include credentials on API calls.
- Handle loading, empty, selected, and failure states.
- Keep audio controls keyboard reachable.

## testing/testing

Use `agent-os/standards/testing/testing.md`.

- Test owner-scoped query behavior where practical.
- Test storage adapter read behavior.
