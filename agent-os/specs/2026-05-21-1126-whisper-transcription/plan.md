# Whisper Transcription Plan

## Goal

Add the first transcription workflow foundation so an authenticated user can start transcription for a saved memo, persist transcript text, and view transcription status/results.

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-05-21-1126-whisper-transcription/` with plan, shape, standards, and references.

## Task 2: Add Transcript Persistence

Add transcript storage linked to `VoiceMemo`.

## Task 3: Add Transcription Workflow Service

Implement a manual transcription command that:

- Checks memo ownership.
- Marks transcription as `PROCESSING`.
- Invokes `TranscriptionPort`.
- Persists transcript text.
- Marks transcription as `COMPLETED` or `FAILED`.

## Task 4: Add Whisper CLI Adapter Foundation

Implement `LocalWhisperAdapter` with configurable command, output directory, timeout, and text output parsing.

## Task 5: Add APIs And UI

Implement:

- `POST /api/memos/{memoId}/transcription`
- `GET /api/memos/{memoId}/transcript`

Add frontend controls to start transcription and show the transcript.

## Acceptance Criteria

- Only the owner can start transcription or read transcript results.
- Transcription status is persisted before and after adapter execution.
- Transcript text is stored separately from audio metadata.
- Missing Whisper CLI failures are reported without deleting memo/audio.
- Normal tests do not require a real Whisper model.
