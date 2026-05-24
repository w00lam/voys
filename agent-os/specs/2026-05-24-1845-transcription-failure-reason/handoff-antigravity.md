# Antigravity Handoff: Transcription Failure Reason

## Branch

`feature/56-transcription-failure-reason`

## Issue

https://github.com/w00lam/voys/issues/56

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

- `backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceFailureReasonTests.java`
- `frontend/src/App.test.tsx`

They fail because transcript responses do not include `failureReason` yet and the frontend failed transcript panel only shows a generic failure message.

## Implementation Requirements

- Add a nullable `failureReason` object to transcript API responses.
- Include `code`, `message`, and `retryable` fields.
- Persist a safe reason when background transcription fails.
- Clear the previous reason when transcription is restarted and the memo enters `PROCESSING`.
- Map known Whisper failures to stable reason codes.
- Keep raw stderr/stdout, stack traces, local paths, and full command details out of API responses.
- Render the safe message in the transcript panel when status is `FAILED`.
- Keep the original audio playable after failure.

## Suggested Failure Mapping

- `Whisper CLI could not be executed.` -> `WHISPER_COMMAND_NOT_FOUND`
- `Whisper transcription timed out.` -> `WHISPER_TIMEOUT`
- `Whisper did not produce a JSON transcript.` -> `WHISPER_EMPTY_OUTPUT`
- Process failures that mention invalid audio, decode errors, or ffmpeg decode failures -> `AUDIO_UNSUPPORTED_OR_INVALID`
- Everything else -> `TRANSCRIPTION_UNEXPECTED_ERROR`

## Notes

- The current `TranscriptionFailedException` message may be useful as an internal signal, but do not expose it directly unless it has been mapped to safe copy.
- Database migration strategy is left to Antigravity based on the current persistence setup.
- Manual retry UX can stay as the existing start transcription button for this slice.
