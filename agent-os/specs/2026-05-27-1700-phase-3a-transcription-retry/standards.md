# Standards: Phase 3A Transcription Retry

## Agent OS References

- `agent-os/product/mission.md`
- `agent-os/product/roadmap.md`
- `agent-os/product/architecture.md`
- `agent-os/product/known-issues.md`
- `agent-os/specs/2026-05-27-1200-phase-2-post-launch/plan.md`
- `agent-os/specs/2026-05-27-1200-phase-2-post-launch/backlog.md`

## Engineering Standards

- Follow `agent-os/standards/backend/spring.md`.
- Follow `agent-os/standards/api/rest.md`.
- Follow `agent-os/standards/frontend/react.md`.
- Follow `agent-os/standards/testing/testing.md`.
- Follow `agent-os/standards/git/branching.md`.

## Implementation Notes

- Keep the retry workflow in the transcription application boundary.
- Do not expose raw Whisper output, paths, command details, or stack traces.
- Do not add a new queue or worker abstraction unless existing boundaries cannot support retry.
- Avoid changing generated note behavior in this slice.
- Existing Phase 2 tests should continue passing.

