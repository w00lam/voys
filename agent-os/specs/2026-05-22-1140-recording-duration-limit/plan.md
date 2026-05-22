# Plan: Recording Duration Limit

## Goal

Enforce the MVP maximum recording duration of 2 hours in both the browser recording UI and backend upload validation.

## Work Mode

This is a TDD handoff branch. Codex owns the Agent OS spec and failing tests. Antigravity should implement production code without weakening the tests.

## Tasks

- Define a 2-hour maximum recording duration.
- Stop browser recording automatically when elapsed time reaches 2 hours.
- Show the maximum duration in the recording UI.
- Reject upload requests with `durationSeconds` greater than 2 hours.
- Reject non-positive duration values when duration is provided.
- Keep existing upload size and content type validation.

## Out Of Scope

- Pause/resume recording.
- Upload progress UI.
- Navigation-loss protection during active recording.
- Real 2-hour audio fixture tests.
