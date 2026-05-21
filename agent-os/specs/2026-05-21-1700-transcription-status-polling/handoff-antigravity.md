# Antigravity Handoff: Transcription Status Polling

## Branch

`feature/21-transcription-status-polling`

## Issue

https://github.com/w00lam/voys/issues/21

## Your Role

Implement production frontend behavior so the tests in this branch pass.

Do not remove or weaken the tests. Adjust test details only if the production behavior becomes clearer and the same user-facing contract remains protected.

## Commands

From `frontend/`:

```powershell
cmd /c npm test
cmd /c npm run build
```

## Expected Failing Tests At Handoff

- `frontend/src/App.test.tsx`

The tests define:

- Selecting a memo loads the current transcript state.
- Starting transcription shows `PROCESSING`.
- The UI polls transcript status and eventually displays completed text.
- The memo list separator should render as `·`, not mojibake.

## Implementation Notes

- Use `getTranscript` for polling.
- Stop polling after `COMPLETED` or `FAILED`.
- Stop polling when the selected memo changes or the component unmounts.
- Refresh memo list status after polling reaches a terminal state.
