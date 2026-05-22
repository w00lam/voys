# Plan: Transcript Segments

## Goal

Store timestamped transcript segments from Whisper output so future search results can jump to the relevant point in a recording.

## Work Mode

This is a TDD handoff branch. Codex owns the Agent OS spec and failing backend tests. Antigravity should implement production code without weakening the tests.

## Tasks

- Extend the transcription port result contract to include timestamped segments.
- Persist transcript segments when a queued transcription job succeeds.
- Preserve segment order from the Whisper result.
- Replace old segments when an existing transcript is regenerated.
- Avoid saving segments when transcription fails.
- Keep Whisper execution behind `TranscriptionPort`.
- Keep normal tests independent from a real Whisper model run.

## Out Of Scope

- Search API timestamp ranking.
- Search result timestamp display.
- Memo detail segment navigation.
- Audio seek from a selected segment.
- Transcript editing.
