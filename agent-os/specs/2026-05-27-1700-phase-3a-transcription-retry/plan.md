# Plan: Phase 3A Transcription Retry

## Objective

Start Phase 3 by making failed transcription recoverable from the existing memo workflow.
Users should be able to retry transcription for an owned memo whose transcription status is `FAILED` without losing the original audio or leaving the memo detail page.

## Issue

- GitHub issue #76: 기능: Phase 3 전사 실패 재시도

## Phase Direction

Phase 3 should strengthen trust in repeated real use before adding broader integrations.
Phase 2 made Voys useful for imported recordings, organization, generated notes, editing, and plain text export.
The next practical gap is recovery: when local Whisper, ffmpeg, Docker, timeout, or environment problems cause a failure, users need a clear way to retry after fixing the problem.

## In Scope

- Add an owner-scoped retry API for failed memo transcriptions.
- Retry only when the memo is currently `FAILED`.
- Reuse the existing asynchronous transcription lifecycle.
- Clear the previous safe failure reason when retry begins.
- Preserve the original audio asset and existing memo metadata.
- Add frontend retry action in the failed transcription state.
- Refresh memo list/detail state after retry starts.
- Keep existing Phase 2 capture, organization, generated-note, edit, and export flows working.

## Out Of Scope

- Automatic retry policies.
- Detailed progress percentage.
- Hosted transcription provider integration.
- Re-transcribing completed memos.
- Retrying memos that are already `PENDING` or `PROCESSING`.
- Automatically regenerating generated notes after retry.
- DOCX, PDF, rich text export, sharing, or collaboration.

## Acceptance Criteria

- A signed-in user can retry transcription for an owned memo in `FAILED` state.
- Retrying transitions the memo back to `PROCESSING` and queues the existing background transcription job.
- The previous failure reason is not shown after retry starts.
- User A cannot retry User B's memo transcription.
- Retrying a memo in `PENDING`, `PROCESSING`, or `COMPLETED` is rejected with a safe API error.
- Frontend shows a retry action only in the failed transcript state.
- Existing recording, import, search, timestamp playback, folder, suggested title, generated note, edit, and plain text export tests continue passing.

