# Shape: Phase 3A Transcription Retry

## User Flow

1. User opens a memo whose transcription status is `FAILED`.
2. The memo detail shows the safe failure reason and a retry action.
3. User clicks retry after fixing the local problem or choosing to try again.
4. Frontend calls a retry endpoint with credentials and CSRF protection.
5. Backend verifies memo ownership and confirms the current status is `FAILED`.
6. Backend clears the previous failure reason, marks the memo `PROCESSING`, and submits the existing transcription job.
7. Frontend shows the normal processing state and continues using the existing polling/status flow.

## API Shape

Use a command endpoint under the transcription resource:

```http
POST /api/memos/{memoId}/transcription/retry
```

The response should match the existing transcription response shape:

```json
{
  "memoId": "33333333-3333-3333-3333-333333333333",
  "status": "PROCESSING",
  "text": null,
  "suggestedTitle": null,
  "segments": [],
  "failureReason": null,
  "updatedAt": null
}
```

## State Rules

- `FAILED` -> `PROCESSING`: allowed through retry.
- `PROCESSING` -> retry: reject as already running.
- `PENDING` -> retry: reject because first transcription should use the existing start action.
- `COMPLETED` -> retry: reject because re-transcription of completed memos is out of scope.
- Ownership must be checked before status-specific behavior is exposed.

## Frontend Shape

- Keep retry inside the selected memo detail transcript panel.
- Show retry next to the safe failure message.
- Hide retry for non-failed transcript states.
- Reuse existing transcript loading and processing display after retry begins.
- Use the same CSRF-aware API client as other POST commands.

