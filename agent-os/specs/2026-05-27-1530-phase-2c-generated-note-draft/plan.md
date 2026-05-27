# Plan: Phase 2C Generated Note Draft

## Objective

Start Phase 2C by generating a first draft note from a completed transcript.
The draft should include a summary, key points, and action items while staying separate from raw transcript data.

## Issue

- GitHub issue #72: 기능: Phase 2C 생성 노트 초안

## Generation Strategy

Use a deterministic local generator for this slice.

- No hosted LLM or network dependency.
- No provider secrets or cost risk.
- Easy to test with stable input and output.
- Keep the generator behind an application boundary so a later local or hosted model adapter can replace it.

## In Scope

- Add generated note persistence separate from transcripts.
- Add an application service that generates notes from completed transcript text.
- Add a local deterministic generator for summary, key points, and action items.
- Add owner-scoped API endpoints to generate and read a memo's generated note.
- Add frontend API helpers and a memo detail UI for generating and viewing the note draft.
- Show safe failures when a note cannot be generated.

## Out Of Scope

- Hosted LLM integration.
- Streaming generation.
- Rich document editor.
- Export.
- Re-generating on every transcript poll automatically.

## Acceptance Criteria

- A signed-in user can generate a draft note for an owned memo with a completed transcript.
- Generated note output is stored separately from raw transcript text.
- The generated note response includes `summary`, `keyPoints`, `actionItems`, `status`, and `updatedAt`.
- A memo without completed transcript text cannot generate a successful note.
- User A cannot generate or read User B's generated note.
- Frontend shows a generate action and renders summary, key points, and action items after success.
- Existing capture, transcription, search, suggested title, folder, and playback flows continue passing.
