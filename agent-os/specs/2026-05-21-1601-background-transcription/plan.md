# Background Transcription TDD Plan

## Goal

Define failing unit tests for moving Whisper transcription out of the HTTP request path and into a background job runner.

## Collaboration Model

- Codex writes the Agent OS spec and failing tests only.
- Antigravity implements production code on the same local branch.
- Antigravity should not weaken, remove, or rewrite the test expectations.
- PR should be opened only after implementation passes the tests.

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-05-21-1601-background-transcription/` with:

- `plan.md`
- `shape.md`
- `standards.md`
- `references.md`

## Task 2: Write Failing Unit Tests

Add unit tests for `TranscriptionWorkflowService` that define the expected background behavior.

Required behaviors:

- Starting transcription marks the memo as `PROCESSING`.
- Starting transcription returns immediately without invoking `TranscriptionPort`.
- Starting transcription enqueues exactly one background job.
- Running the queued job invokes `TranscriptionPort`, stores transcript text, and marks the memo `COMPLETED`.
- If the queued job fails, the memo becomes `FAILED` and no transcript is saved.
- Starting transcription for a memo owned by another user fails with `MemoNotFoundException`.
- Starting transcription while already `PROCESSING` fails without enqueuing another job.

## Task 3: Hand Off To Antigravity

Antigravity should implement the production code to pass the tests, then run:

```text
backend/gradlew.bat test
cmd /c npm run build
```

## Acceptance Criteria

- Tests clearly express the desired behavior.
- Production behavior can be implemented without changing the public REST API.
- No actual Whisper CLI or filesystem access is required by the unit tests.
