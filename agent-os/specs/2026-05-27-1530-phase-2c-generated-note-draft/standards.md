# Standards: Phase 2C Generated Note Draft

## Backend

- Keep generated note logic in a notes application boundary.
- Keep raw transcripts immutable from generated note edits or regeneration.
- Preserve owner-scoped memo lookup before read/write operations.
- Use safe API errors for missing transcript or invalid generation state.
- Keep generator deterministic for this slice.

## Frontend

- Keep controls compact and in the memo detail flow.
- Use existing CSRF-aware API helpers.
- Show loading and failure states without hiding transcript content.
- Do not add explanatory marketing copy.

## Testing

- Backend tests should cover generation from completed transcript, missing transcript rejection, stored-note read, and owner-scoped lookup.
- Frontend API tests should cover generated note GET and POST endpoints.
- Frontend UI tests should cover generating and rendering summary, key points, and action items.
- Existing Phase 2A and Phase 2B tests should continue passing.
