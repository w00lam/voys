# Memo Library Playback Plan

## Goal

Let authenticated users browse their saved voice memos and play back the original stored audio through owner-checked backend APIs.

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-05-21-1044-memo-library-playback/` with plan, shape, standards, and references.

## Task 2: Add Memo Query APIs

Implement:

- `GET /api/memos`
- `GET /api/memos/{memoId}`

Responses should include memo status and audio metadata needed by the current UI.

## Task 3: Add Authenticated Audio Playback API

Implement:

- `GET /api/memos/{memoId}/audio`

The endpoint should verify the memo owner before opening the stored local file.

## Task 4: Add Frontend Memo Library UI

Add a minimal authenticated library:

- Load memo list after login and after upload.
- Select a memo.
- Fetch audio with credentials.
- Render an audio player for the selected memo.

## Acceptance Criteria

- Users only see their own memos.
- Users can select a saved memo and play its original audio.
- Audio paths are not exposed directly to the client.
- Transcription and search remain out of scope.
