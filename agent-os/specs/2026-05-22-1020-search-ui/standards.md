# Standards: Search UI

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Use the shared API client and include credentials.
- Debounce free-text search input.
- Show enough context to explain why a result matched.
- Keep buttons and inputs keyboard accessible.
- Do not hide existing memo browsing or playback controls.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Mock API calls.
- Use fake timers for debounce behavior.
- Do not require a real backend.
