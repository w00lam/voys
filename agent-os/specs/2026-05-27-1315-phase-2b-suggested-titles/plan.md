# Plan: Phase 2B Suggested Titles

## Goal

Help users improve memo names after transcription without taking control away from them.
When a transcript completes, Voys should derive a safe suggested title, expose it separately from the current memo title, and let the user adopt it intentionally.

## Issue

- GitHub issue #68: 기능: Phase 2B 추천 제목

## Work Mode

This is a Codex + Antigravity TDD handoff branch.
Codex owns this focused spec, failing tests, and the Antigravity prompt.
Antigravity should implement production code without weakening the tests.

## Scope

- Derive a suggested title from completed transcript text.
- Store the suggested title separately from the memo's accepted title.
- Expose the suggested title from the transcript response.
- Show the suggested title in the memo detail transcript area.
- Let the user adopt the suggested title through the existing title update API.
- Never overwrite the current memo title automatically.

## Out Of Scope

- LLM or hosted model integration.
- Tags, folders, or collections.
- Summaries, key points, action items, generated documents, or export.
- Transcript editing.
- Social login or integrations.

## Acceptance Criteria

- Completing transcription can set `suggestedTitle` while leaving `title` unchanged.
- Suggested title derivation uses a simple safe heuristic based on transcript text.
- `GET /api/memos/{memoId}/transcript` returns `suggestedTitle`.
- Frontend displays the suggested title when present.
- Clicking the adopt action sends the suggested title through `PATCH /api/memos/{memoId}`.
- Existing manual title editing, import, recording, search, timestamp jump, and playback flows remain intact.
