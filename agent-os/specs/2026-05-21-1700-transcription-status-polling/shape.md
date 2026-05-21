# Shape: Transcription Status Polling

## Current Problem

The backend now starts transcription in a background worker. The frontend receives the initial `PROCESSING` response, but it does not automatically refresh the transcript until the job finishes.

## Desired Flow

1. User selects a saved memo.
2. Frontend loads memo detail, audio, and current transcript status.
3. User starts transcription.
4. Frontend displays `PROCESSING`.
5. Frontend polls `GET /api/memos/{memoId}/transcript` on a bounded interval.
6. Polling stops when status becomes `COMPLETED` or `FAILED`.
7. On `COMPLETED`, the transcript text appears without manual refresh.
8. On `FAILED`, a user-visible failure state appears and the original memo remains selectable.

## Expected Implementation Direction

- Keep polling local to the selected memo view for now.
- Stop polling when the selected memo changes, the user logs out, or a terminal status arrives.
- Refresh the memo list after status changes so list metadata stays current.
- Keep API access through `features/memos/api.ts`.
- Keep tests mocked at the API boundary; do not require a real backend.

## Polling Interval

Use a short bounded interval appropriate for MVP UI feedback. Tests advance fake timers by 6 seconds and expect at least one follow-up transcript read after the initial start response.
