# Standards: Transcription Failure Reason

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Follow `agent-os/standards/api/rest.md`.
- Keep Whisper execution details behind `TranscriptionPort`.
- Persist failure status and failure reason in the application layer after the port fails.
- Map low-level exceptions to stable, safe failure codes.
- Clear stale failure reasons when a new transcription attempt starts.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Keep the memo detail view usable when transcription fails.
- Show visible failure text, not only color or icon state.
- Prefer a concise reason plus next step.
- Do not expose raw technical details in the UI.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Backend tests should not invoke real Whisper.
- Backend tests should verify persisted/returned safe failure reasons.
- Frontend tests should verify the failed transcript panel renders the safe message.
