# Backlog: Phase 2 Post-Launch

## Phase 2A: Capture Expansion

### Audio Import API

- Define accepted content types and file size limits.
- Add authenticated audio file upload endpoint.
- Validate owner, file presence, content type, size, and duration when available.
- Store imported audio through the existing storage port.
- Create a memo with source metadata and initial title.
- Start or enqueue transcription using the existing workflow.
- Return safe error envelopes for validation failures.

### Memo Title Editing API

- Add title validation rules.
- Add authenticated memo metadata update endpoint.
- Enforce user ownership.
- Update memo list/detail response shapes if needed.
- Ensure title search uses the edited title.

### Frontend Import UX

- Add an import action to the memo library.
- Add file picker flow with supported format copy.
- Show upload progress and upload failure states.
- Insert or refresh the created memo after upload.
- Reuse memo detail transcription status UI.

### Frontend Title Editing UX

- Add inline or focused title edit control in memo detail.
- Keep keyboard and screen-reader behavior clear.
- Show validation and save failures without losing the current title.
- Reflect saved title in memo list and search results.

## Phase 2B: Library Organization

### Suggested Titles

- Define title suggestion source and fallback behavior.
- Persist suggested title separately from accepted title.
- Let users keep the current title, adopt the suggestion, or edit manually.
- Avoid overwriting user-edited titles automatically.

### Lightweight Organization

- Compare tags, folders, and collections against real imported memo workflows.
- Select one first organization primitive.
- Add backend ownership and validation rules.
- Add memo list filtering or grouping.
- Keep search compatible with the selected organization model.

## Phase 2C: Generated Notes

### Summary And Action Extraction

- Decide generation adapter strategy.
- Generate summary, key points, and action items from completed transcripts.
- Persist generated output separately from raw transcript data.
- Show generation status and safe failures.

### Generated Document View

- Create a readable generated note view for the full recording.
- Allow editing generated documentation without editing raw transcript/audio.
- Add export for transcript or generated note after the document shape is stable.

## Phase 2D: Reach And Integrations

### Browser Reach

- Validate Safari recording constraints.
- Decide whether to support additional browser recording formats directly or through transcoding.
- Preserve current Chrome/Edge behavior.

### Account And Integrations

- Revisit social login when signup friction is measured as a blocker.
- Evaluate calendar or learning-tool integrations after generated notes are valuable enough to sync.

## Cross-Slice Verification

- Existing browser recording flow still works.
- Existing transcription status polling still reaches terminal states.
- Search still finds memo titles and transcript text.
- Timestamp search results still open the memo and seek audio.
- User A cannot access User B's memo, audio, transcript, generated note, or organization metadata.
- Safe errors never expose local paths, stack traces, command output, secrets, or storage keys.

## Open Questions

- What exact upload size limit matches the current 2-hour recording policy across supported formats?
- Should imported audio start transcription automatically, matching browser recording behavior, or wait for an explicit start action?
- Which organization primitive should Phase 2B choose after imported memo usage exists?
- What generation strategy should Phase 2C use?
- Which user feedback signal should trigger Safari recording, social login, or integrations?
