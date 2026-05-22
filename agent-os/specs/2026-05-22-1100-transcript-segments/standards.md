# Standards: Transcript Segments

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Keep controllers out of this slice unless needed by production compilation.
- Keep Whisper-specific parsing inside the local Whisper adapter.
- Do not hold a transaction open while the Whisper CLI runs.
- Save transcript text and segments in the short transaction after Whisper completes.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Use fake transcription ports for workflow tests.
- Do not invoke Whisper CLI in unit tests.
- Protect both success and failure behavior.
- Tests should prove segment order and timestamp values are preserved.
