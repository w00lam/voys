# Plan: Search UI

## Goal

Define the first frontend search experience so authenticated users can search memo titles and transcript snippets from the web UI.

## Work Mode

This is a TDD handoff branch. Codex owns the Agent OS spec and failing tests. Antigravity should implement production frontend code without weakening the tests.

## Tasks

- Add a search input for authenticated users.
- Debounce free-text search input before calling the backend.
- Call `GET /api/search?q={query}` through the shared API client.
- Render search result title, match type, snippet, and transcription status.
- Open the selected memo when a search result is clicked.
- Do not call the search API for blank queries.
- Preserve the existing memo list and playback workflows.
- Ensure memo metadata separators render as `·`, not mojibake.

## Out Of Scope

- Timestamp-based result navigation.
- Audio seek from search results.
- Search result pagination.
- Backend search changes.
