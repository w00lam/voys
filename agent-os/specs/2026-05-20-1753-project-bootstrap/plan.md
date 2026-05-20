# Project Bootstrap Plan

## Goal

Create the initial runnable project foundation for Voys so future feature specs can build on a consistent Spring Boot backend, React + Vite frontend, PostgreSQL development setup, and Agent OS standards.

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-05-20-1753-project-bootstrap/` with:

- `plan.md` - this implementation plan.
- `shape.md` - shaping notes and decisions.
- `standards.md` - applicable standards.
- `references.md` - project context and references.

## Task 2: Bootstrap Spring Boot Backend

Create a backend project using:

- Java 21
- Spring Boot 3.x
- Gradle
- Spring Web
- Spring Validation
- Spring Security
- Spring Data JPA
- PostgreSQL driver

Initial backend scope:

- Add root Gradle files and backend module/project structure.
- Create base package `com.voys`.
- Add initial package folders for `shared`, `identity`, `memo`, `transcription`, and `search`.
- Add a health endpoint under `/api/health`.
- Configure application profiles for local development.

## Task 3: Bootstrap React + Vite Frontend

Create a frontend project using:

- React
- Vite
- TypeScript

Initial frontend scope:

- Add feature-oriented folder structure.
- Add a shared API client wrapper that uses `credentials: "include"`.
- Add a minimal app shell.
- Add a basic health/API connectivity check screen or development-only indicator.

## Task 4: Add Local PostgreSQL Development Setup

Add local infrastructure setup:

- Docker Compose file for PostgreSQL.
- Local environment example values.
- Backend datasource configuration for local development.

Keep secrets and real local config out of Git.

## Task 5: Add Session Auth Foundation

Add the first authentication foundation:

- Spring Security configuration.
- Password encoder bean.
- Session-cookie-oriented defaults.
- CORS/CSRF development notes for the Vite origin.
- Stub or minimal endpoints for `/api/me`, signup, login, and logout if appropriate for the bootstrap scope.

This task should avoid overbuilding full identity workflows unless needed to verify the foundation.

## Task 6: Add Storage And Transcription Port Skeletons

Add interfaces and basic skeletons:

- `StoragePort`
- `LocalFileStorageAdapter`
- `TranscriptionPort`
- `LocalWhisperAdapter`

The bootstrap can include placeholders without invoking real Whisper yet. The goal is to establish boundaries.

## Task 7: Add Basic Tests And Verification

Add enough verification to prove the foundation works:

- Backend context or health endpoint test.
- Frontend build/test script if available.
- Manual verification notes for running backend, frontend, and PostgreSQL locally.

## Acceptance Criteria

- Backend can start locally.
- Frontend can start locally.
- PostgreSQL local setup is documented/configured.
- `/api/health` or equivalent endpoint responds.
- Frontend can call the backend during local development.
- Agent OS standards are referenced by the implementation structure.
- No generated secrets, local storage files, build outputs, or audio files are committed.

