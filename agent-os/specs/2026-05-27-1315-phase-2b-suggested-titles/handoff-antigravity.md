# Antigravity Handoff: Phase 2B Suggested Titles

## Mission

Implement Phase 2B suggested titles using the failing tests added by Codex on this branch.

## Issue

https://github.com/w00lam/voys/issues/68

## Spec

`agent-os/specs/2026-05-27-1315-phase-2b-suggested-titles/`

## Required Behavior

- Derive a suggested title when transcription completes.
- Store suggested title separately from the accepted memo title.
- Return `suggestedTitle` from transcript responses.
- Do not overwrite current memo titles automatically.
- Let frontend users adopt suggested titles through the existing title update path.
- Preserve Phase 2A import/title editing and all MVP transcript/search/playback behavior.

## Tests Added By Codex

- `backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceSuggestedTitleTests.java`
- `frontend/src/App.test.tsx`
- `frontend/src/features/memos/api.test.ts`

## Verification

Run:

```powershell
cd backend
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1\jbr'
.\gradlew.bat test
```

```powershell
cd frontend
cmd /c npm test -- --run
cmd /c npm run build
```
