# Plan: Memo And Transcript Search

## Goal

Define the first MVP search slice so an authenticated user can search their memo titles and completed transcript text.

## Work Mode

This is a TDD handoff branch. Codex owns the Agent OS spec and failing tests. Antigravity should implement production backend code without weakening the tests.

## Tasks

- Add `GET /api/search?q={query}`.
- Search only resources owned by the authenticated user.
- Match memo titles.
- Match transcript text.
- Return result objects with memo id, title, match type, snippet, and transcription status.
- Reject blank queries with a domain/API validation error.
- Keep timestamp segment search out of this slice.

## Out Of Scope

- Frontend search UI.
- Transcript segment timestamps.
- Jump-to-audio behavior.
- Full-text search engine integration.
- Pagination beyond a simple MVP limit.
