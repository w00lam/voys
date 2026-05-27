# Antigravity Handoff: Phase 2A Capture Expansion

## Mission

Implement Phase 2A production code using the failing tests added by Codex on this branch.

## Issue

https://github.com/w00lam/voys/issues/66

## Spec

`agent-os/specs/2026-05-27-1245-phase-2a-capture-expansion/`

## Required Behavior

- Import existing audio files through authenticated multipart upload.
- Support at least MP3 and WAV imports in addition to the existing WebM recording path.
- Reject unsupported imported file types before storage.
- Create imported memo titles from sanitized filenames.
- Rename owned memos through an authenticated metadata update.
- Reject blank or overlong memo titles.
- Preserve existing browser recording upload, transcription status polling, transcript display, search, timestamp jump, and playback behavior.

## Tests Added By Codex

- `backend/src/test/java/com/voys/memo/application/AudioFileImportServiceTests.java`
- `backend/src/test/java/com/voys/memo/application/MemoLibraryServiceTitleUpdateTests.java`
- `backend/src/test/java/com/voys/memo/api/AudioFileImportControllerTests.java`
- `backend/src/test/java/com/voys/memo/api/MemoControllerTitleUpdateTests.java`
- `frontend/src/App.test.tsx`
- `frontend/src/features/memos/api.test.ts`

## Verification

Run:

```powershell
cd backend
.\gradlew.bat test
```

```powershell
cd frontend
cmd /c npm test
cmd /c npm run build
```
