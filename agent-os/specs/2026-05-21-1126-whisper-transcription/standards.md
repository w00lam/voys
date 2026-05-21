# Standards for Whisper Transcription

## backend/spring

Use `agent-os/standards/backend/spring.md`.

- Keep Whisper behind `TranscriptionPort`.
- Do not hold a DB transaction open during CLI execution.
- Persist status transitions around long-running work.
- Keep owner checks server-side.

## api/rest

Use `agent-os/standards/api/rest.md`.

- Use explicit command endpoint for starting transcription.
- Expose status/result through memo/transcript APIs.
- Return safe error envelopes.

## frontend/react

Use `agent-os/standards/frontend/react.md`.

- Show long-running transcription state clearly.
- Keep transcript read-only.
- Handle failures without hiding the original recording.

## testing/testing

Use `agent-os/standards/testing/testing.md`.

- Do not require real Whisper in normal tests.
- Unit test status transitions where practical.
