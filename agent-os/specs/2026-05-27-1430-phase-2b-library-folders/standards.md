# Standards: Phase 2B Library Folders

## Backend

- Keep organization metadata in memo application/domain code, not controllers.
- Preserve owner-scoped lookups before reads and writes.
- Return safe validation errors for invalid folder names.
- Keep `PATCH /api/memos/{memoId}` backwards compatible with title updates.
- Keep search behavior unchanged unless explicitly filtering memo lists.

## Frontend

- Keep the folder UI compact and part of the current memo workspace.
- Use form controls with accessible labels for folder editing and filtering.
- Preserve the selected memo after folder updates where practical.
- Do not hide memos permanently when filters change; provide a clear all-folder option.

## Testing

- Backend tests should cover trim, clear, length validation, and owner-scoped updates.
- Backend tests should cover folder-filtered memo listing.
- Frontend API tests should cover folder update and folder-filtered list requests.
- Frontend UI tests should cover assigning a folder and filtering the memo list.
- Existing import/title/suggested-title/transcription/search tests should continue passing.
