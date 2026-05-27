# Plan: Phase 2A Audio Import And Title Management

## Goal

Make Voys useful for recordings captured outside the in-browser recorder.
Users should be able to upload an existing audio file, transcribe it through the same background workflow, search the completed transcript, play the original audio, and rename the memo title.

## Work Mode

This is a design-only PR.
Do not implement production code, do not add failing tests yet, and do not auto-merge.
The next implementation PR should use the Codex + Antigravity TDD handoff workflow.

## Scope

- Add an authenticated audio file upload entry point for existing recordings.
- Accept a small, explicit set of audio formats that the backend can validate and the browser can play.
- Create a user-owned memo from the uploaded file.
- Store uploaded audio through the existing storage boundary.
- Reuse the existing asynchronous transcription workflow.
- Reuse the existing transcription status, transcript display, search, timestamp jump, and audio playback surfaces.
- Add manual memo title editing for user-owned memos.
- Keep safe validation and error messages for unsupported files, oversized files, invalid duration, and unauthorized access.

## Out Of Scope

- Safari in-browser recording support.
- Drag-and-drop folder import or multi-file batch import.
- Automatic audio transcoding.
- Speaker labels, diarization, or transcript quality improvements.
- Suggested titles, summaries, action items, or generated document views.
- Transcript editing.
- Export.
- Calendar, learning-tool, or cloud storage integrations.
- Social login.

## Success Criteria

- A signed-in user can upload a supported audio file and see it appear in their memo library.
- Uploaded audio is owned by the current user and is not visible to other users.
- The uploaded memo can enter the same transcription status lifecycle as browser-recorded memos.
- Completed transcripts from uploaded audio are searchable.
- Timestamped transcript results can open the memo and seek the audio.
- The user can rename a memo title without changing transcript or audio data.
- Unsupported files are rejected with user-safe API errors.
- Existing browser recording flows continue to work.
