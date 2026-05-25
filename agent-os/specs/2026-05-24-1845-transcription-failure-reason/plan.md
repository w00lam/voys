# Plan: Transcription Failure Reason

## Goal

Make transcription failures understandable without exposing unsafe backend details.
When Whisper fails, users should see a safe reason and a practical next step instead of only a generic `FAILED` status.

## Work Mode

This is a TDD handoff branch.
Codex owns the Agent OS spec and failing tests.
Antigravity should implement production code without weakening the tests.

## Tasks

- Persist a safe transcription failure reason when the background transcription job fails.
- Return the failure reason from `GET /api/memos/{memoId}/transcript` and the start transcription response when the status is `FAILED`.
- Display the safe failure reason in the memo detail transcript panel.
- Keep raw command output, stack traces, local filesystem paths, and full environment details out of API responses.
- Clear the previous failure reason when transcription is started again.
- Preserve the original memo and audio after failure.

## Out Of Scope

- Automatic retry.
- Manual retry UX beyond the existing start transcription command.
- Detailed progress percentage for long recordings.
- Changing Whisper model selection from the UI.
- Hosted speech-to-text fallback.
