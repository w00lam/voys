# Antigravity Handoff: Recording Duration Limit

## Branch

`feature/35-recording-duration-limit`

## Issue

https://github.com/w00lam/voys/issues/35

## Your Role

Implement production code so the tests in this branch pass.

Do not remove or weaken the tests. Adjust tests only if the same user-facing contract remains protected.

## Commands

From `backend/`:

```powershell
.\gradlew.bat test
```

From `frontend/`:

```powershell
cmd /c npm test
cmd /c npm run build
```

## Expected Failing Tests At Handoff

- `backend/src/test/java/com/voys/memo/application/RecordingUploadServiceTests.java`
- `frontend/src/App.test.tsx`

They fail because backend upload validation does not check duration yet, and the frontend recorder does not auto-stop at 2 hours.

## Implementation Requirements

- Reject provided `durationSeconds` values above 7200.
- Reject provided `durationSeconds` values less than or equal to 0.
- Keep `durationSeconds = null` accepted for compatibility.
- Automatically stop the browser recorder when elapsed time reaches 7200 seconds.
- Show that the maximum recording duration is 2 hours in the UI.
- Ensure auto-stop does not repeatedly call `stop()`.

## Notes

- Do not add pause/resume behavior in this slice.
- Do not add upload progress in this slice.
