# Standards: Phase 2C Note Edit And Export

## Backend

- Keep edit/export logic in `notes` application code where possible.
- Preserve owner-scoped memo lookup before note or transcript access.
- Do not mutate raw transcripts when editing generated notes.
- Return safe validation errors.
- Keep export format plain text for this slice.

## Frontend

- Use existing CSRF-aware API helpers.
- Avoid adding a separate notes workspace.
- Keep editable fields accessible with labels.
- Keep export actions visible only when data exists.

## Testing

- Backend tests should cover edit success, blank summary rejection, generated-note export, transcript export, and owner-scoped lookup.
- Frontend API tests should cover PATCH and text export calls.
- Frontend UI tests should cover editing and saving a generated note.
- Existing Phase 2 tests should continue passing.
