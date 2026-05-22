# Antigravity Handoff: Transcript Segment List

## Branch

`feature/37-transcript-segment-list`

## Issue

https://github.com/w00lam/voys/issues/37

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

- `backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceSegmentReadTests.java`
- `frontend/src/App.test.tsx`

They fail because transcript responses do not include segments yet and the frontend transcript panel does not render clickable segment timestamps.

## Implementation Requirements

- Add ordered transcript segments to `TranscriptionResponse`.
- Return an empty segment list when no transcript exists.
- Add a repository lookup for segments ordered by position.
- Render timestamped segment controls in the transcript panel.
- Clicking a segment seeks the selected audio to `startSeconds`.
- Do not auto-play audio after clicking a segment.

## Notes

- Search result timestamp jump must keep working.
- Transcript editing is not part of this slice.
