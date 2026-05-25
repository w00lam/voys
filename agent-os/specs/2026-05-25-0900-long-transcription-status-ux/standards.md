# Standards: Long Transcription Status UX

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Status changes must be visible in text, not only color.
- Keep the original audio controls visible and reachable.
- Keep the transcript read-only.
- Preserve existing polling behavior.

## Testing

- Follow `agent-os/standards/testing/testing.md`.
- Mock API calls.
- Do not require real audio, a real backend, or real Whisper.
- Protect the `PROCESSING` guidance copy with a frontend test.

## Product

- Follow `agent-os/product/roadmap.md` Phase 1.5.
- Align with `agent-os/product/known-issues.md` for long recording limitations.
