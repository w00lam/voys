# Voys

Voys is a web-based voice memo workspace for recording meetings, lectures, interviews, and study sessions, then transcribing, searching, and revisiting the important moments later.

The Phase 1 MVP focuses on a browser-first workflow:

- Email/password sign-up and login with server-side sessions.
- Browser recording with WebM/Opus in Chrome or Edge.
- 2-hour maximum recording duration.
- Local filesystem audio storage.
- Local open-source Whisper transcription through a CLI adapter.
- Background transcription status updates.
- Read-only transcript display.
- Timestamped transcript segments.
- Search across memo titles and completed transcript text.
- Search and transcript timestamp jumps into the original audio.

## Tech Stack

- Frontend: React, TypeScript, Vite, MediaRecorder API.
- Backend: Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA, Gradle.
- Database: PostgreSQL for local development and production target.
- Transcription: local Whisper CLI behind `TranscriptionPort`.
- Storage: local filesystem behind `StoragePort`.
- Project docs: Agent OS under `agent-os/`.

## Repository Layout

```text
backend/              Spring Boot REST API
frontend/             React + Vite web app
agent-os/product/     Mission, roadmap, architecture, verification docs
agent-os/standards/   Engineering standards
agent-os/specs/       Feature specs and handoff notes
.github/workflows/    CI, auto PR, and bot review workflows
```

## Requirements

- Java 21
- Node.js and npm
- Docker Desktop or another Docker-compatible runtime
- GitHub CLI for repository automation
- Whisper CLI available on the machine when running real transcription

## Local Setup

Create local environment variables from the example if needed:

```powershell
Copy-Item .env.example .env
```

Start PostgreSQL:

```powershell
docker compose up -d postgres
```

Run the backend:

```powershell
cd backend
.\gradlew.bat bootRun
```

Run the frontend in another terminal:

```powershell
cd frontend
cmd /c npm install
cmd /c npm run dev
```

The Vite dev server proxies `/api` requests to the backend at `http://localhost:8080`.

## Configuration

Useful environment variables are listed in `.env.example`.

Important defaults:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/voys`
- `VOYS_STORAGE_ROOT=storage/audio`
- `VOYS_FRONTEND_ORIGIN=http://localhost:5173`
- `VOYS_WHISPER_COMMAND=whisper`
- `VOYS_WHISPER_OUTPUT_ROOT=storage/transcripts`
- `VOYS_WHISPER_TIMEOUT_SECONDS=7200`

Local audio and transcript output directories should stay out of Git.

## Verification

Backend tests:

```powershell
cd backend
.\gradlew.bat test
```

Frontend tests and build:

```powershell
cd frontend
cmd /c npm test
cmd /c npm run build
```

Manual MVP verification:

- See `agent-os/product/mvp-verification.md`.

## Product And Architecture Docs

- `agent-os/product/mission.md`
- `agent-os/product/roadmap.md`
- `agent-os/product/architecture.md`
- `agent-os/product/tech-stack.md`
- `agent-os/product/mvp-verification.md`

## Development Workflow

This repository uses Agent OS and a Codex + Antigravity collaboration workflow.

- Use GitHub issues before feature work.
- Branch from latest `main`.
- Use `feature/{issue-number}-{short-description}` for feature branches.
- Use `chore/{issue-number}-{short-description}` for documentation or maintenance.
- Codex writes Agent OS specs and failing tests for Antigravity TDD handoffs when the work changes user-facing behavior or crosses layers.
- PRs are opened automatically and merge decisions remain manual.

See `AGENTS.md` for repository-specific collaboration rules.
