# Software Architecture

## Context

This document captures the initial architecture direction for the project. The product is a web-based voice memo workspace for professionals and students who record meetings, lectures, interviews, or study sessions and want to automatically organize and search them later. The mission and roadmap are still being shaped, so the architecture starts with conservative defaults that are easy to evolve.

The first implementation target is a Spring-based backend. The system should start as a small modular monolith rather than a distributed system. This keeps the first version simple while still leaving room to separate modules or services later if real product needs justify it.

## Goals

- Define the product boundaries clearly.
- Keep the first version small enough to build and validate.
- Choose technologies that match the team's operating style.
- Document important tradeoffs before implementation begins.
- Prefer boring, proven infrastructure until the product shape demands more.
- Keep domain logic independent from transport, persistence, and framework details where practical.

## System Overview

The initial system is a React + Vite web frontend backed by a Spring Boot REST API. The browser client handles recording, upload progress, transcription status polling, and search interactions. The backend owns session-based authentication, authorization, core business logic, persistence, local file storage coordination, and local open-source Whisper transcription through explicit adapter boundaries.

High-level shape:

```text
React + Vite web frontend with browser recording
  -> Spring Boot REST API
    -> Application services
      -> Domain model
      -> Repository ports
      -> Integration ports
    -> Database / local filesystem storage adapter
    -> Local Whisper worker
```

The application should be deployable as a single service. Background jobs, event handling, or additional services should be introduced only when there is a clear product or operational reason.

## Core Domains

Initial product domains:

- Identity and access: users, authentication, authorization, account ownership.
- Browser recording: client-side recording session lifecycle and completed audio upload.
- Voice memo workspace: user-owned collection of meeting, lecture, interview, and study recordings with transcripts and note metadata.
- Transcription workflow: audio processing, transcription status, transcript storage, and failure handling.
- Search and organization: full-text search, tags, folders, dates, speakers, or other lightweight organization features.
- Notifications and integrations: optional outbound communication or third-party integration surfaces.

Each domain should have a clear owner package and avoid leaking persistence entities directly into API responses.

## Major Components

Initial backend components:

- Web recording client: React UI for starting, pausing, stopping, and submitting recordings.
- API layer: REST controllers, request DTOs, response DTOs, validation, and HTTP status mapping.
- Authentication layer: email/password sign-up and login, password hashing, server-side sessions, HttpOnly/SameSite cookies, CSRF protection, and user-scoped authorization.
- Application layer: use case services that coordinate domain operations, transactions, and authorization checks.
- Domain layer: domain objects, policies, value objects, and business rules.
- Persistence layer: Spring Data repositories and database mappings.
- Integration layer: adapters for local Whisper execution, local filesystem storage, email, or other external systems.
- Configuration layer: security, database, serialization, observability, and environment-specific configuration.

Package structure should follow product modules first, with technical subpackages inside each module when useful:

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

For the first small version, fewer packages are fine. Add structure when it protects clarity, not as ceremony.

## Data Model

Use a relational data model with PostgreSQL as the production default.

Initial modeling rules:

- Use stable primary keys for persisted aggregate roots.
- Keep audit fields where useful: `created_at`, `updated_at`, and optional `deleted_at`.
- Model ownership explicitly for user-scoped resources.
- Avoid storing secrets or tokens in plain text.
- Prefer database constraints for invariants the database can enforce reliably.

The first concrete entity model should be added after the product mission and MVP workflows are defined.

Likely early entities:

- User
- Credential
- RecordingSession
- VoiceMemo
- AudioAsset
- Transcript
- TranscriptSegment
- Tag or Collection

Audio files should be stored on the local filesystem for the MVP. The database should store metadata such as owner, original filename, content type, size, duration when available, storage key/path, and processing status. Raw audio bytes should not be stored in PostgreSQL.

## External Integrations

The MVP should use local open-source Whisper transcription rather than a hosted speech-to-text API. Whisper execution should live outside the core domain model behind a transcription adapter.

Preferred shape:

```text
Spring Boot transcription service
  -> TranscriptionPort
    -> LocalWhisperAdapter
      -> Whisper CLI process
```

This keeps Spring Boot responsible for product workflows, user data, and persistence while the Python/Whisper runtime handles CPU/GPU-intensive transcription.

MVP transcription flow:

```text
Recording uploaded
  -> Spring Boot stores audio file through StoragePort
  -> LocalFileStorageAdapter writes audio under the configured storage root
  -> Spring Boot stores audio file metadata
  -> Spring Boot creates transcription job
  -> LocalWhisperAdapter invokes Whisper CLI with the audio file path
  -> Whisper writes text/JSON output
  -> Spring Boot parses the output
  -> Transcript and TranscriptSegment records are saved
  -> Memo becomes searchable
```

This is intentionally simple for the MVP. If transcription volume grows, the CLI adapter can be replaced with a long-running worker, job queue, or sidecar service behind the same `TranscriptionPort`.

Storage should also stay behind a port:

```text
Spring Boot memo service
  -> StoragePort
    -> LocalFileStorageAdapter
      -> local storage root
```

The local storage root should be configurable per environment and excluded from Git.

Browser recording should be treated as a client capability. The backend should not depend on browser-specific details beyond receiving supported audio formats and metadata.

Frontend/backend authentication notes:

- The frontend should call authenticated APIs with browser credentials included.
- Cookies should be HttpOnly and SameSite-aware.
- Local development may require CORS and CSRF configuration for the Vite dev server origin.
- JWT should not be used for the MVP unless a mobile app, third-party client, or server-to-server integration requires it later.

MVP recording constraints:

- Maximum recording duration: 2 hours.
- Initial browser target: Chrome/Edge.
- Initial audio format: `audio/webm;codecs=opus`.
- Recordings should be saved with an automatic temporary title based on date and time.
- Transcription should run asynchronously after upload.
- File size limits should be validated against 2-hour WebM/Opus recordings during implementation.
- The UI should make long-running transcription status visible to the user.
- Transcript segments should include timestamps so search results can jump to the relevant point in the recording.
- The memo detail view should support playing the original audio from a selected transcript timestamp.
- MVP transcripts should be read-only. Human editing should be introduced later at the generated-document layer rather than as raw transcript/audio editing.

When integrations are added:

- Hide vendor-specific code behind an adapter.
- Keep credentials in environment variables or a secret manager, never in Git.
- Make outbound calls observable with clear logging and error handling.
- Design retries and idempotency for operations that can be safely repeated.
- Treat audio upload, transcription, and transcript persistence as separately recoverable steps.
- Store recording status separately from transcription status so failed transcription does not lose the original memo.

## Deployment

Start with a single deployable Spring Boot service and a PostgreSQL database.

Recommended early environments:

- Local: application plus PostgreSQL through Docker Compose or local database.
- Development: one shared environment for integration testing.
- Production: one application deployment and managed PostgreSQL.

CI/CD should initially run:

- compile/build
- unit tests
- integration tests when they exist
- static checks or formatting when configured

## Quality Attributes

- Reliability: Keep the MVP small, observable, and easy to roll back. Add retries only for operations that are safe to retry.
- Security: Validate all input, hash passwords, use HttpOnly/Secure/SameSite cookies, enable CSRF protection where needed, keep secrets out of source control, and use Spring Security before any user-specific data is exposed.
- Performance: Prefer simple database queries and pagination for list endpoints. Optimize after measuring real bottlenecks.
- Maintainability: Keep business rules in application/domain code rather than controllers. Favor clear module boundaries over premature microservices.
- Testability: Unit test domain rules and application services. Add integration tests for persistence and important API flows.

## Architecture Decisions

| Date | Decision | Status | Notes |
| --- | --- | --- | --- |
| 2026-05-20 | Initialize Agent OS project structure | Accepted | Repository is ready for product and architecture planning. |
| 2026-05-20 | Start with a Spring Boot modular monolith | Proposed | Keeps the first version simple while preserving module boundaries. |
| 2026-05-20 | Prefer PostgreSQL for production persistence | Proposed | Relational default fits most MVP workflows and works well with Spring Data. |
| 2026-05-20 | Keep external integrations behind adapters | Proposed | Prevents vendor APIs from leaking into core business logic. |
| 2026-05-20 | Build the first client as a web application | Accepted | The initial product target is browser-based, not a native mobile app. |
| 2026-05-20 | Include browser-based recording in the MVP | Accepted | Users should be able to record directly in the web app before transcription and search. |
| 2026-05-20 | Include simple authentication in the MVP | Accepted | Personal recordings and transcripts require user-specific data separation from the start. |
| 2026-05-20 | Use local open-source Whisper transcription for the MVP | Accepted | Avoids hosted speech-to-text API dependency and keeps transcription under local control. |
| 2026-05-20 | Invoke Whisper through a CLI adapter for the MVP | Accepted | Simplest local integration path; can later evolve into a worker, queue, or sidecar. |
| 2026-05-20 | Store MVP audio files on the local filesystem | Accepted | Fastest initial implementation; `StoragePort` keeps future object storage migration possible. |
| 2026-05-20 | Use Gradle for the Spring project | Accepted | Gradle fits Spring Boot 3.x and leaves room for future multi-module structure. |
| 2026-05-20 | Use WebM/Opus for MVP browser recordings | Accepted | Chrome/Edge-first format with good compression for long recordings. |
| 2026-05-20 | Use Spring Security session authentication for the MVP | Accepted | Browser-first product with sensitive personal recordings is simpler and safer with HttpOnly cookie sessions. |
| 2026-05-20 | Use React + Vite for the MVP web frontend | Accepted | Interactive recording, upload progress, status polling, and search UI benefit from a dedicated frontend. |
| 2026-05-20 | Use automatic temporary titles for the MVP | Accepted | Users can save quickly without waiting for title generation or full document processing. |
| 2026-05-20 | Support timestamp-based audio verification | Accepted | Search results should help users verify the relevant moment without replaying long recordings. |
| 2026-05-20 | Keep transcript editing out of the MVP | Accepted | Editing is more useful after generated documentation exists, not at the raw transcript/audio layer. |

## Open Questions

- What is the smallest useful MVP?
- What constraints should shape the technical stack?
- Should JWT be introduced later for mobile apps, third-party clients, or server-to-server integrations?
- What maximum upload size matches the selected browser audio format and 2-hour recording limit?
