# Standards for Background Transcription TDD

## backend/spring

Use `agent-os/standards/backend/spring.md`.

- Keep transactions short.
- Do not hold a transaction open while Whisper runs.
- Keep owner checks server-side.
- Keep Whisper behind `TranscriptionPort`.

## testing/testing

Use `agent-os/standards/testing/testing.md`.

- Unit tests should use fake ports and repositories.
- Unit tests must not invoke real Whisper.
- Tests should protect status transitions and duplicate-start behavior.

## api/rest

Use `agent-os/standards/api/rest.md`.

- Preserve the existing command endpoint shape.
- Long-running work should expose status rather than blocking.
