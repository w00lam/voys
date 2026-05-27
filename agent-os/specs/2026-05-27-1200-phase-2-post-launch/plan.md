# Plan: Phase 2 Post-Launch

## Goal

Define the full Phase 2 direction before starting implementation slices.
Phase 2 should turn the MVP from a browser-recording demo into a repeatable personal voice memo workspace where users can bring in real recordings, find them again, organize them lightly, and prepare for generated notes later.

## Work Mode

This is a design-only PR.
Do not implement production code, do not add failing tests yet, and do not auto-merge.
After this Phase 2 spec is accepted, each implementation slice should use the Codex + Antigravity TDD handoff workflow.

## Phase 2 Scope

- Bring recordings captured outside the app into the memo library.
- Let users rename memos so the library is understandable after repeated use.
- Improve memo organization with the smallest useful metadata before adding heavier generated-note workflows.
- Prepare for suggested titles, summaries, action items, generated documents, editing, and export without committing to a model/provider too early.
- Keep existing MVP workflows working: recording, upload, transcription status, transcript display, search, timestamp jump, and audio playback.

## Phase 2 Non-Goals

- Replacing the MVP transcription architecture.
- Building a full document editor before generated-note behavior is validated.
- Adding third-party integrations before the core capture/review loop is stronger.
- Treating social login as a prerequisite for product value.
- Solving every browser recording format before file import and review workflows are useful.

## Proposed Slice Sequence

1. Phase 2A: Capture Expansion
   - Audio file upload for recordings captured outside the app.
   - Manual memo title editing.
   - Reuse existing transcription, search, timestamp jump, and playback flows.
2. Phase 2B: Library Organization
   - Suggested titles after transcription.
   - Lightweight tags, folders, or collections after title editing and imported memo volume exist.
3. Phase 2C: Generated Notes
   - Summaries, key points, and action items.
   - Generated document view and editing.
   - Export transcripts or generated notes.
4. Phase 2D: Reach And Integrations
   - Safari and broader browser recording support.
   - Social login if onboarding friction becomes the bottleneck.
   - Calendar or learning-tool integrations after core notes are valuable.

## Phase 2 Success Criteria

- Users can add real-world recordings that were not recorded inside the app.
- Users can keep a growing memo library understandable through names and lightweight organization.
- Search and timestamp playback remain the fastest way to verify captured audio.
- Generated-note work starts only after the capture and organization foundation is usable.
- Each slice is small enough to review, test, and merge manually.
