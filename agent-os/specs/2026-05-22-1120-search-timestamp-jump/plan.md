# Plan: Search Timestamp Jump

## Goal

Let users open a transcript search result at the matching timestamp instead of manually scanning a long recording.

## Work Mode

This is a TDD handoff branch. Codex owns the Agent OS spec and failing tests. Antigravity should implement production code without weakening the tests.

## Tasks

- Add a nullable `segmentStartSeconds` value to search results.
- Return `segmentStartSeconds` for transcript segment matches.
- Keep title matches working with no timestamp.
- Render a compact timestamp label in transcript search results.
- When a timestamped search result is clicked, open the memo and seek the audio element to that timestamp.
- Do not auto-play audio after seeking.

## Out Of Scope

- Transcript segment list UI.
- Clicking transcript text to seek audio.
- Audio waveform or highlighted transcript rendering.
- Advanced search ranking.
