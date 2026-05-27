# Antigravity Handoff: Phase 3A Transcription Retry

Implement Phase 3A transcription retry using the failing tests added by Codex on this branch.

## Issue

- GitHub issue #76: 기능: Phase 3 전사 실패 재시도

## Summary

Add a safe retry path for failed transcriptions. A user should be able to retry only an owned memo in `FAILED` state. Retry should clear the previous failure reason, transition the memo into the existing asynchronous `PROCESSING` lifecycle, and keep the original audio and memo metadata intact.

## Do Not Implement

- Automatic retries.
- Hosted transcription provider integration.
- Re-transcription of completed memos.
- Progress percentages.
- Generated-note regeneration.
- DOCX/PDF/rich text export or sharing.

