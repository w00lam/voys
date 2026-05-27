# Product Roadmap

## Phase 1: MVP

- Email and password sign-up/login.
- Browser-based audio recording for meetings, lectures, interviews, and study sessions.
- Chrome/Edge-first recording support using WebM/Opus.
- Recording duration limit of up to 2 hours for MVP.
- Save each recording with an automatic temporary title based on date and time.
- Upload recorded audio from the browser to the backend after recording is complete.
- Transcribe saved recordings into searchable text asynchronously.
- Display transcription status and the final transcript.
- Search across memo titles and transcript text.
- Store transcript segments with timestamps.
- Open a search result and jump to the relevant transcript segment.
- Play the original audio from the relevant timestamp for quick verification.
- Treat transcription as read-only in the MVP.

## Phase 1.5: MVP Stabilization

- Show safe, user-understandable transcription failure reasons instead of only a generic `FAILED` state.
- Document Whisper model configuration, model quality tradeoffs, and local transcription limitations.
- Explain that Docker first build, first model download, and first transcription can take a long time.
- Improve long-recording transcription status UX so users understand that `PROCESSING` can last several minutes.
- Maintain known issues for Docker, Whisper, browser support, long recordings, and transcription quality.
- Strengthen the manual MVP verification checklist for shared local demos.
- Collect early real-use feedback and turn it into candidate Phase 2 priorities.

## Phase 2: Post-Launch

- Define Phase 2 as post-launch work that improves repeated personal use before broad integrations.
- Phase 2A proposed first slice:
  - Audio file upload for recordings captured outside the app.
  - Manual memo title editing so users can name imported and browser-recorded memos.
  - Reuse the existing transcription, transcript, search, timestamp jump, and playback workflows for imported audio.
- Phase 2B proposed slice:
  - Suggested titles after transcription completes.
  - Allowing users to keep, adopt, or edit suggested titles.
  - Tags, folders, or collections for organization.
- Phase 2C proposed slice:
  - Summaries, key points, and action items.
  - Generated document view for the full recording.
  - Editing generated documentation after transcription/summarization.
  - Export transcripts or notes.
- Later Phase 2 candidates:
  - Safari and broader browser audio format support.
  - Speaker labels or transcript segmentation improvements.
  - Social login.
  - Calendar or learning-tool integrations.
