# Plan: Phase 2C Note Edit And Export

## Objective

Finish the practical generated-note loop by letting users edit generated note drafts and export either the generated note or transcript as plain text.

## Issue

- GitHub issue #74: 기능: Phase 2C 생성 노트 편집과 내보내기

## In Scope

- Update generated note summary, key points, and action items through an owner-scoped API.
- Preserve raw transcript text when generated notes are edited.
- Export generated note text.
- Export raw transcript text.
- Add frontend memo detail controls for editing and saving generated notes.
- Add frontend export actions for generated note and transcript text.

## Out Of Scope

- Rich text editing.
- DOCX, PDF, or markdown files.
- Sharing or collaboration.
- Hosted LLM regeneration.
- Version history.

## Acceptance Criteria

- A signed-in user can edit an owned memo's generated note.
- Blank summary is rejected safely.
- Key points and action items can be replaced as text lists.
- User A cannot edit or export User B's generated note or transcript.
- Export returns readable plain text.
- Frontend renders editable generated note fields and saves them.
- Frontend offers transcript and generated note export actions.
