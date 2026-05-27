# Antigravity Handoff: Phase 2C Generated Note Draft

## Goal

Implement the first Phase 2C generated-note draft slice using the failing tests added by Codex on this branch.

## Spec

`agent-os/specs/2026-05-27-1530-phase-2c-generated-note-draft/`

## Requirements

- Create a generated note boundary separate from raw transcript persistence.
- Generate a deterministic local note draft from completed transcript text.
- Store generated note summary, key points, action items, status, safe failure, and updated time.
- Add owner-scoped GET and POST endpoints under `/api/memos/{memoId}/generated-note`.
- Add frontend API helpers and memo detail UI to generate and render draft notes.
- Preserve existing capture, transcription, suggested title, folder, search, and playback behavior.

## Verification

Run:

```powershell
cd backend
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1\jbr'
.\gradlew.bat test
cd ..\frontend
npm test -- --run
npm run build
```

Do not skip tests.
