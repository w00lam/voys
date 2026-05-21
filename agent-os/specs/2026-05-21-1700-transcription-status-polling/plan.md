# Plan: Transcription Status Polling

## Goal

Define the frontend behavior for automatically refreshing background transcription status until the transcript reaches a terminal state.

## Work Mode

This is a TDD handoff branch. Codex owns the spec and failing tests. Antigravity should implement the production frontend behavior without weakening these tests.

## Tasks

- Add frontend test tooling with Vitest and React Testing Library.
- Add App-level tests for the selected memo transcription flow.
- Verify starting transcription shows `PROCESSING`.
- Verify the UI polls `GET /api/memos/{memoId}/transcript` until `COMPLETED`.
- Verify the final transcript text appears without manual refresh.
- Verify memo list metadata does not render mojibake separators.

## Out Of Scope

- Search implementation.
- Transcript timestamp segmentation.
- Backend API changes.
- Real Whisper execution in tests.
