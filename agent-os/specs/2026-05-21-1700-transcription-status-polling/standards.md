# Standards: Transcription Status Polling

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Treat transcription as asynchronous.
- Poll until `COMPLETED` or `FAILED`.
- Stop polling when the selected memo is no longer active.
- Show visible text for `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED`.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Mock API calls.
- Use fake timers for polling behavior.
- Do not depend on a real microphone, backend, or Whisper runtime.
