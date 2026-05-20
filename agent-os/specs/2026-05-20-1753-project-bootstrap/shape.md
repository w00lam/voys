# Project Bootstrap - Shaping Notes

## Scope

Create the initial runnable project foundation for Voys:

- Spring Boot 3.x backend with Java 21 and Gradle.
- React + Vite frontend with TypeScript.
- PostgreSQL local development setup.
- Session-cookie authentication foundation.
- Initial module/package structure aligned with the architecture.
- Basic backend/frontend development connection.

This spec does not implement the full recording, transcription, search, or memo workflows. It prepares the foundation for those features.

## Decisions

- The product is a web-based voice memo workspace for professionals and students.
- The backend is a Spring Boot REST API.
- The frontend is React + Vite.
- Authentication should use Spring Security server sessions, not JWT, for the MVP.
- Audio storage will be local filesystem behind `StoragePort`.
- Transcription will use local open-source Whisper through a CLI adapter behind `TranscriptionPort`.
- PostgreSQL is the production-oriented default database.
- Browser recording target is Chrome/Edge with `audio/webm;codecs=opus`.

## Context

- **Product docs:** `agent-os/product/mission.md`, `agent-os/product/roadmap.md`, `agent-os/product/tech-stack.md`, `agent-os/product/architecture.md`
- **Visuals:** None.
- **References:** No existing application code yet.
- **Product alignment:** Bootstrap should preserve the ability to build recording, async transcription, transcript search, and timestamp-based audio verification.

## Standards Applied

- `backend/spring` - Spring Boot structure, layering, persistence, session auth, ports/adapters.
- `api/rest` - REST API shape, error response envelope, auth semantics, upload/status/search conventions.
- `frontend/react` - React + Vite structure, session cookie API calls, recording UI standards.
- `testing/testing` - unit/integration/manual verification expectations.

