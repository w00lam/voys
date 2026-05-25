# Shape: Transcription Failure Reason

## Current Problem

The MVP records `FAILED` when Whisper transcription fails, but the user cannot tell whether the issue is local setup, model download, timeout, invalid audio, empty output, or an unexpected error.
This makes the Docker demo and early feedback loop harder because users do not know what to fix or report.

## Desired Flow

1. User selects a saved memo.
2. User starts transcription.
3. Backend marks the memo `PROCESSING` and runs Whisper in the background.
4. Whisper or storage access fails.
5. Backend marks the memo `FAILED` and stores a safe failure reason.
6. Frontend polling receives `FAILED` with the safe reason.
7. The transcript panel displays the reason and keeps the original audio playable.
8. If the user starts transcription again, the previous failure reason is cleared while the memo is `PROCESSING`.

## API Contract

Transcript responses should include a nullable failure reason:

```json
{
  "memoId": "33333333-3333-3333-3333-333333333333",
  "status": "FAILED",
  "text": null,
  "segments": [],
  "failureReason": {
    "code": "WHISPER_COMMAND_NOT_FOUND",
    "message": "Whisper CLI is not installed or not available to the backend process.",
    "retryable": true
  },
  "updatedAt": "2026-05-24T09:45:00Z"
}
```

For non-failed statuses, `failureReason` should be `null`.

## Failure Reason Categories

Use stable codes and safe user-facing messages.

- `WHISPER_COMMAND_NOT_FOUND`: Whisper command cannot be executed.
- `WHISPER_TIMEOUT`: Whisper exceeded `VOYS_WHISPER_TIMEOUT_SECONDS`.
- `WHISPER_EMPTY_OUTPUT`: Whisper completed but did not produce usable transcript output.
- `AUDIO_UNSUPPORTED_OR_INVALID`: Audio cannot be processed by Whisper or ffmpeg.
- `TRANSCRIPTION_UNEXPECTED_ERROR`: Any other unexpected transcription error.

## Data Shape

Prefer storing failure metadata on the memo/transcription workflow state rather than in transcript text.
The current MVP has a single transcription state per memo, so a nullable failure code/message on the memo or a small memo-linked transcription state record are both acceptable.

## Safety

- Do not return stack traces.
- Do not return raw stderr/stdout from Whisper.
- Do not return local filesystem paths.
- Do not return full shell commands with user-specific paths.
- Backend logs may keep detailed diagnostics for developers.
