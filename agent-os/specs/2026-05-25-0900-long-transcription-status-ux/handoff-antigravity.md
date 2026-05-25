# Antigravity Handoff: Long Transcription Status UX

## Branch

`feature/58-long-transcription-status-ux`

## Issue

https://github.com/w00lam/voys/issues/58

## Your Role

Implement production frontend code so the tests in this branch pass.

Do not remove or weaken the tests. Adjust tests only if the same user-facing contract remains protected.

## Commands

From `frontend/`:

```powershell
cmd /c npm test
cmd /c npm run build
```

Backend changes are not expected for this slice. If you do touch backend code, run backend tests too:

```powershell
cd backend
.\gradlew.bat test
```

## Expected Failing Tests At Handoff

- `frontend/src/App.test.tsx`

It fails because the transcript panel does not yet show clear guidance while transcript status is `PROCESSING`.

## Implementation Requirements

- Render visible guidance when selected memo transcript status is `PROCESSING`.
- Include this sentence: `긴 녹음이나 첫 전사는 몇 분 이상 걸릴 수 있습니다.`
- Keep original audio controls visible and playable while processing.
- Keep polling behavior unchanged.
- Keep completed transcript, failed reason, segment list, and empty transcript states working.

## Notes

- Do not add progress percentages in this slice.
- Do not change Whisper or backend job execution.
- Keep the UI copy concise.
