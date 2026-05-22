# Plan: Transcript Segment List

## Goal

Show timestamped transcript segments in the memo detail view and let users jump the audio to a selected segment.

## Work Mode

This is a TDD handoff branch. Codex owns the Agent OS spec and failing tests. Antigravity should implement production code without weakening the tests.

## Tasks

- Include ordered transcript segments in the transcript detail API response.
- Keep the existing full transcript text response.
- Render timestamped transcript segments in the frontend transcript panel.
- Seek the memo audio to the clicked segment start time.
- Do not auto-play audio after segment click.
- Preserve search result timestamp jump behavior.

## Out Of Scope

- Transcript editing.
- Speaker labels.
- Active segment highlighting while audio plays.
- Waveform UI.
