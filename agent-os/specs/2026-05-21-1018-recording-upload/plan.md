# Recording Upload Plan

## Goal

Implement the first browser recording storage slice so an authenticated user can record WebM/Opus audio in the browser, upload it, and receive a user-owned voice memo record with audio metadata.

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-05-21-1018-recording-upload/` with:

- `plan.md` - implementation plan.
- `shape.md` - shaping notes and decisions.
- `standards.md` - applicable standards.
- `references.md` - project context and references.

## Task 2: Add Memo And Audio Persistence

Add initial memo persistence:

- `VoiceMemo` owned by `UserAccount`.
- `AudioAsset` owned by a memo.
- Temporary title generated from current date/time.
- Recording and transcription status fields for future workflow steps.

## Task 3: Add Local Storage Port

Add storage abstraction and local filesystem adapter:

- `StoragePort`
- `LocalFileStorageAdapter`
- Generated storage keys under a configurable root.

## Task 4: Add Recording Upload API

Implement:

- `POST /api/memos/recordings`

The API should:

- Require an authenticated session.
- Accept multipart `audio` file.
- Validate WebM audio content type.
- Enforce a maximum upload size.
- Store the file through `StoragePort`.
- Persist memo/audio metadata.
- Return memo summary DTO.

## Task 5: Add Browser Recording UI

Add a simple authenticated recording UI:

- Check `audio/webm;codecs=opus` support.
- Start/stop recording with `MediaRecorder`.
- Show elapsed time.
- Upload after recording stops.
- Show created memo title and status.

## Task 6: Add Tests And Verification

Add focused backend tests for upload service validation and storage behavior where practical.

## Acceptance Criteria

- Unauthenticated users cannot upload recordings.
- Authenticated users can upload a WebM audio file.
- Upload creates a user-owned memo with a temporary title.
- Audio bytes are stored under the configured local storage root.
- Audio metadata is persisted separately from raw audio bytes.
- Frontend can record, stop, and upload from Chrome/Edge-compatible browsers.
- Transcription remains out of scope for this slice.
