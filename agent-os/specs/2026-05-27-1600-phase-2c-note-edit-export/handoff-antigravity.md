# Antigravity Handoff: Phase 2C Note Edit And Export

## Goal

Implement editable generated notes and plain text exports using the failing tests added by Codex.

## Spec

`agent-os/specs/2026-05-27-1600-phase-2c-note-edit-export/`

## Requirements

- Add owner-scoped generated note edit support.
- Validate non-blank summary.
- Replace key points and action items from request lists.
- Export generated note as plain text.
- Export transcript as plain text.
- Add frontend API helpers and memo detail UI for edit/save/export.
- Preserve existing capture, organization, transcription, generated note draft, search, and playback behavior.

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
