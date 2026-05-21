# Standards: Memo And Transcript Search

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Keep ownership checks server-side.
- Do not expose JPA entities directly.
- Use a domain-specific exception for invalid search queries.
- Map expected validation failures to the shared API error envelope.

## REST API

- Follow `agent-os/standards/api/rest.md`.
- Use `GET /api/search?q={query}`.
- Keep response fields lower camel case.
- Return safe snippets only.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Unit test application service behavior.
- Unit test controller delegation to the authenticated user's id.
- Do not require a real full-text search engine.
