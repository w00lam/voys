# References: Phase 3A Transcription Retry

## Product Context

- `agent-os/product/known-issues.md` documents retry-related local Whisper and long-running transcription limitations.
- Phase 1.5 introduced safe failure reasons.
- Phase 2 kept original audio and memo metadata usable after transcription-related workflows.

## Related Code

- `backend/src/main/java/com/voys/transcription/application/TranscriptionWorkflowService.java`
- `backend/src/main/java/com/voys/transcription/api/TranscriptionController.java`
- `backend/src/main/java/com/voys/memo/infrastructure/persistence/VoiceMemo.java`
- `frontend/src/features/memos/api.ts`
- `frontend/src/App.tsx`

## Related Tests

- `backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceBackgroundTests.java`
- `backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceFailureReasonTests.java`
- `frontend/src/features/memos/api.test.ts`
- `frontend/src/App.test.tsx`

