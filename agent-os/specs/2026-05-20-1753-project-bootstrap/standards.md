# Standards for Project Bootstrap

The following standards apply to this work.

---

## backend/spring

Use `agent-os/standards/backend/spring.md`.

Key points:

- Java 21, Spring Boot 3.x, Gradle.
- Package by product module first.
- Keep controllers, application services, domain, and infrastructure separate.
- Use Spring Security session authentication for the MVP.
- Keep storage and Whisper behind ports/adapters.
- Do not run long Whisper work inside short request/transaction flows.

---

## api/rest

Use `agent-os/standards/api/rest.md`.

Key points:

- Prefix application APIs with `/api`.
- Use explicit request and response DTOs.
- Use a consistent error envelope.
- Use session cookie authentication.
- Plan upload and transcription APIs as asynchronous workflows.

---

## frontend/react

Use `agent-os/standards/frontend/react.md`.

Key points:

- React + Vite with TypeScript.
- Feature-oriented frontend structure.
- API calls include browser credentials.
- Do not store session IDs or JWTs in JavaScript-accessible storage.
- Prepare for MediaRecorder-based recording UI.

---

## testing/testing

Use `agent-os/standards/testing/testing.md`.

Key points:

- Unit test domain and application behavior.
- Integration test auth and API flows.
- Mock MediaRecorder in frontend tests.
- Do not require a real Whisper model run in normal tests.

