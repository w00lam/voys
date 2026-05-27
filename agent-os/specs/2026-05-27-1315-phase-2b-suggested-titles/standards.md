# Standards: Phase 2B Suggested Titles

## Backend

- Follow `agent-os/standards/backend/spring.md`.
- Keep title suggestion logic deterministic and local for this slice.
- Store the suggestion separately from accepted memo title metadata.
- Keep title validation aligned with existing manual title editing.
- Do not add provider-specific integration code.

## API

- Follow `agent-os/standards/api/rest.md`.
- Extend existing transcript response rather than adding a separate endpoint.
- Use `PATCH /api/memos/{memoId}` to adopt the suggested title.

## Frontend

- Follow `agent-os/standards/frontend/react.md`.
- Keep suggested title UI compact and inside the existing memo detail workflow.
- Avoid forcing title changes automatically.

## Tests

- Backend tests should cover derivation, response exposure, and no automatic title overwrite.
- Frontend tests should cover rendering and adopting a suggested title.
- Existing import/title edit/transcription tests should continue passing.
