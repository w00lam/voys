# Plan: Long Transcription Status UX

## Goal

Make long-running transcription feel understandable during Phase 1.5 demos.
When a memo remains in `PROCESSING`, users should see clear copy explaining that long recordings, Docker first runs, and first Whisper model downloads can take several minutes.

## Work Mode

This is a TDD handoff branch.
Codex owns the Agent OS spec and failing frontend tests.
Antigravity should implement production code without weakening the tests.

## Tasks

- Add user-visible `PROCESSING` guidance in the memo detail transcript panel.
- Explain that long recordings or first transcriptions can take several minutes.
- Keep the original audio playable while transcription is processing.
- Keep the existing polling behavior unchanged.
- Keep completed, failed, and empty transcript states working.

## Out Of Scope

- Progress percentages.
- Queue position or worker observability.
- Backend job instrumentation.
- Retry UX.
- Changing polling interval.
- Whisper model selection from the UI.
