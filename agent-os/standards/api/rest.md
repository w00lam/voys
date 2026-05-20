# REST API Standards

## Scope

These standards apply to the Spring Boot REST API consumed by the React + Vite frontend.

## API Shape

- Prefix application APIs with `/api`.
- Use nouns for resources.
- Use HTTP methods consistently:
  - `GET` reads resources.
  - `POST` creates resources or starts commands.
  - `PATCH` updates part of a resource.
  - `DELETE` removes or archives resources.
- Keep command-like endpoints explicit when a workflow is not simple CRUD.

Examples:

```text
POST /api/auth/signup
POST /api/auth/login
POST /api/auth/logout
GET  /api/me

GET  /api/memos
POST /api/memos/recordings
GET  /api/memos/{memoId}
PATCH /api/memos/{memoId}

GET  /api/memos/{memoId}/transcript
GET  /api/search?q={query}
```

## Authentication

- Use Spring Security session cookies for MVP authentication.
- Authenticated browser requests from the frontend must include credentials.
- Configure CORS and CSRF intentionally for the Vite development origin.
- Return `401 Unauthorized` when the user is not authenticated.
- Return `403 Forbidden` when the user is authenticated but not allowed to access a resource.

## Request And Response DTOs

- Use explicit request DTOs for input.
- Use explicit response DTOs for output.
- Do not expose JPA entities directly.
- Validate request DTOs with Spring Validation annotations where possible.
- Keep API field names stable and use lower camel case JSON fields.

## Error Responses

Return a consistent error envelope:

```json
{
  "code": "memo.not_found",
  "message": "Memo was not found.",
  "details": {}
}
```

Guidelines:

- `code` is stable and machine-readable.
- `message` is safe for users.
- `details` is optional and should not expose secrets or internal paths.
- Validation errors should identify invalid fields.

## Uploads

- Accept MVP browser recordings as `audio/webm;codecs=opus`.
- Validate content type, size, authenticated owner, and expected workflow state.
- Store uploaded audio through `StoragePort`.
- Persist upload metadata before starting transcription.
- Do not run Whisper CLI inside the upload request if it may block the response for a long time.

## Long-Running Work

- Transcription is asynchronous.
- APIs should expose status rather than blocking until completion.
- Use explicit statuses such as `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED`.
- Failed transcription should not delete the memo or original audio.

## Search

- Search should initially cover memo titles and transcript text.
- Search results should include enough context to navigate to the matching memo and transcript segment.
- Search results that match transcript text should include timestamp information when available.

## Pagination

- Use pagination for list endpoints that can grow.
- Start with simple page/size parameters unless product needs require cursor pagination.
- Define default and maximum page sizes.

