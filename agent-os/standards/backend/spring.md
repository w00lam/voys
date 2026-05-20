# Spring Backend Standards

## Scope

These standards apply to the Spring Boot backend for Voys.

The backend is responsible for authentication, authorization, REST APIs, domain workflows, persistence, local audio storage coordination, and local Whisper CLI transcription coordination.

## Baseline

- Use Java 21.
- Use Spring Boot 3.x.
- Use Gradle.
- Use PostgreSQL for the main database.
- Use Spring Security session authentication for the MVP.
- Keep local audio files outside Git under a configurable storage root.

## Package Structure

Organize code by product module first, then by technical role.

```text
com.voys
  shared
  identity
    api
    application
    domain
    infrastructure
  memo
    api
    application
    domain
    infrastructure
  transcription
    application
    domain
    infrastructure
  search
    api
    application
    domain
    infrastructure
```

Use fewer packages for very small features, but do not put controller, persistence, and business logic into one class.

## Layering

- Controllers handle HTTP concerns only: request parsing, validation entry points, current user lookup, response mapping, and status codes.
- Application services coordinate use cases, transactions, authorization checks, persistence ports, storage ports, and transcription ports.
- Domain objects hold business rules and state transitions where those rules are more than simple CRUD.
- Infrastructure adapters implement persistence, local filesystem storage, Whisper CLI execution, email, or other external details.
- Do not return JPA entities directly from controllers.
- Do not call framework-specific APIs from domain objects unless there is a strong reason.

## Persistence

- Use Spring Data JPA for relational persistence unless a feature clearly needs another approach.
- Keep JPA entities separate from API request/response DTOs.
- Store raw audio files on the filesystem, not in PostgreSQL.
- Store audio metadata in PostgreSQL, including owner, storage key/path, content type, size, duration when available, and processing status.
- Use database constraints for uniqueness, ownership relationships, and non-null invariants that the database can enforce.
- Add audit fields where useful: `createdAt`, `updatedAt`, and optional `deletedAt`.

## Transactions

- Put transaction boundaries on application service methods.
- Keep transactions short.
- Do not hold a database transaction open while running Whisper CLI or doing long file operations.
- Persist job/status transitions before and after long-running transcription work.

## Authentication And Authorization

- Use email/password authentication for the MVP.
- Hash passwords with a strong Spring Security-supported password encoder.
- Use server-side sessions with HttpOnly, Secure, SameSite-aware cookies.
- Authorize all user-owned resources by owner.
- Never rely on client-provided user IDs for ownership decisions.
- JWT should be deferred until mobile apps, third-party clients, or server-to-server integrations require it.

## Ports And Adapters

- Use a port interface when integrating with local filesystem storage or Whisper CLI.
- Initial storage port: `StoragePort` with a `LocalFileStorageAdapter`.
- Initial transcription port: `TranscriptionPort` with a `LocalWhisperAdapter`.
- Keep adapter-specific paths, commands, and process details outside application services.

## Errors

- Prefer domain-specific exceptions for expected business failures.
- Map exceptions to consistent API error responses in one place.
- Avoid leaking stack traces, filesystem paths, command arguments, or secrets in API responses.

## Testing

- Unit test domain rules and application services.
- Integration test persistence mappings and important API flows.
- Test adapters with focused integration tests where practical.
- Do not require a real long Whisper run for normal unit tests; use a fake transcription adapter.

