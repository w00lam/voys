# Plan: Phase 2A Capture Expansion

## Goal

Start Phase 2 by allowing users to bring existing audio files into Voys and rename memos.
Imported audio should behave like browser-recorded audio after creation: it belongs to the current user, can be transcribed through the existing workflow, can be searched after transcription, and can be played from timestamped results.

## Issue

- GitHub issue #66: 기능: Phase 2A 캡처 확장

## Work Mode

This is a Codex + Antigravity TDD handoff branch.
Codex owns this focused spec, failing tests, and the Antigravity prompt.
Antigravity should implement production code without weakening the tests.

## Tasks

- Add an authenticated audio file import endpoint for existing recordings.
- Accept a small supported audio set beyond browser WebM recordings, starting with MP3 and WAV in tests.
- Reject unsupported files with safe API errors before storage.
- Create imported memos with sanitized filename-derived titles when possible.
- Fall back to the temporary title generator when a filename cannot produce a usable title.
- Store imported audio through the existing storage boundary.
- Keep imported memos in the existing transcription lifecycle.
- Add authenticated memo title editing.
- Validate edited titles as non-blank and within the memo title length limit.
- Enforce memo ownership for title edits.
- Add frontend import controls and title editing controls in the existing memo workspace.

## Out Of Scope

- Batch import.
- Drag-and-drop folder import.
- Audio transcoding.
- Safari in-browser recording support.
- Suggested titles.
- Tags, folders, or collections.
- Summaries, action items, generated documents, editing generated notes, or export.
- Transcript editing.
- Social login or integrations.

## Acceptance Criteria

- A signed-in user can import an MP3 file and see the created memo in the library.
- Imported memo titles default to a sanitized filename without the extension.
- Unsupported file types are rejected before storage.
- Imported audio uses the same transcript status, transcript display, search, timestamp jump, and playback flows as existing memos.
- A signed-in user can rename a memo they own.
- Blank or overlong titles are rejected safely.
- User A cannot rename User B's memo.
- Existing browser recording upload behavior remains intact.
