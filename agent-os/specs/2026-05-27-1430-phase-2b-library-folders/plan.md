# Plan: Phase 2B Library Folders

## Objective

Complete the Phase 2B lightweight organization slice by selecting folders as the first organization primitive.
Users should be able to assign one optional folder to a memo, see folder metadata in the memo library, and filter the memo list by folder.

## Issue

- GitHub issue #70: 기능: Phase 2B 폴더 기반 라이브러리 정리

## Primitive Decision

Choose folders before tags or collections.

- Folders match the immediate imported-recording workflow: a memo belongs to one practical context such as `Work`, `Study`, or `Interviews`.
- Folders can be represented as nullable memo metadata without adding a many-to-many tagging model.
- Folders support list filtering now while leaving tags and richer collections available for later feedback-driven work.
- Collections are deferred because they imply heavier curation, ordering, sharing, or generated-note grouping semantics.

## In Scope

- Add nullable folder metadata to memos.
- Validate folder names as blank-or-null to clear, otherwise trimmed and within a conservative length limit.
- Update folder metadata only through owner-scoped memo access.
- Return folder metadata from memo list and memo detail responses.
- Allow memo list filtering by folder.
- Add frontend controls to set or clear a selected memo folder.
- Add frontend folder filter controls in the memo library.
- Preserve existing title editing, suggested title adoption, import, transcription, playback, and search behavior.

## Out Of Scope

- Multi-tag assignment.
- Nested folders.
- Collection sharing, ordering, or generated-note grouping.
- Search syntax for folder queries.
- Phase 2C generated notes.

## Acceptance Criteria

- A signed-in user can assign a folder to an owned memo.
- Folder names are trimmed before persistence.
- Blank folder input clears the folder.
- Overlong folder names are rejected safely.
- User A cannot update or filter User B's memo folder metadata.
- Memo list and detail responses include `folder`.
- Memo list can be filtered to a selected folder.
- Frontend can set, clear, and filter by folder without breaking existing memo workflows.
