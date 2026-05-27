# Antigravity Handoff: Phase 2A Audio Import And Title Management

## Mission

Implement Phase 2A with TDD.
Start from the Agent OS spec in this folder, write failing tests first, then implement the smallest production changes needed to pass them.

## Spec

`agent-os/specs/2026-05-27-1200-phase-2a-audio-import-title-management/`

## Work Expectations

- Keep changes incremental and aligned with existing backend/frontend structure.
- Preserve browser recording behavior.
- Reuse the existing storage and transcription workflow boundaries.
- Add title editing as memo metadata only.
- Keep API errors safe and user-readable.
- Do not implement deferred Phase 2 items.

## Initial Test Targets

- Backend authenticated audio import success.
- Backend unsupported file type rejection.
- Backend ownership isolation for imported memos.
- Backend title update success and validation failures.
- Frontend import success and upload failure states.
- Frontend title edit success and validation failure states.
- Regression coverage for existing transcription polling behavior where touched.

## Deferred

- Suggested titles.
- Tags, folders, or collections.
- Summaries, action items, and generated document views.
- Export.
- Social login.
- Safari in-browser recording support.
