# Antigravity Handoff: Phase 2B Library Folders

## Goal

Implement Phase 2B library folders using the failing tests added by Codex on this branch.

## Spec

`agent-os/specs/2026-05-27-1430-phase-2b-library-folders/`

## Requirements

- Add nullable folder metadata to memos.
- Keep folder updates owner-scoped.
- Trim non-blank folder names.
- Clear folder metadata when folder is blank or null.
- Reject folder names longer than 80 characters.
- Return folder in memo summary/detail/update responses.
- Support `GET /api/memos?folder=...`.
- Add frontend folder edit and library filter controls.
- Preserve Phase 2A capture expansion and Phase 2B suggested title behavior.

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
