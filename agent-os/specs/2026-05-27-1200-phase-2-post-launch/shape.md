# Shape: Phase 2 Post-Launch

## Current Product State

Phase 1 and Phase 1.5 make the MVP demoable:

- Users can sign up, log in, record audio in Chrome or Edge, and save memos.
- Uploaded browser recordings can be transcribed asynchronously.
- Users can see transcription status, safe failure reasons, transcript text, transcript segments, search results, timestamp jumps, and audio playback.
- Known local Whisper and Docker constraints are documented for demos.

The remaining gap is repeated personal use.
Users often already have audio files from meetings, lectures, interviews, calls, or study tools.
They also need recognizable memo names and eventually organization and generated notes.

## Phase 2 Product Shape

Phase 2 should build outward from the existing memo workflow rather than introduce a separate workspace.
Every new capability should either improve capture, review, organization, or reuse of the same memo records.

```text
Capture
  -> browser recording
  -> imported audio file

Processing
  -> existing async transcription
  -> status, safe failure, transcript, segments

Review
  -> memo detail
  -> search
  -> timestamp playback

Organization
  -> editable title
  -> suggested title
  -> tags/folders/collections

Generated notes
  -> summary/key points/action items
  -> generated document view
  -> editing and export
```

## Slice 2A: Capture Expansion

Start with audio import and manual title editing because they unlock real recordings while reusing the most MVP infrastructure.

Desired outcomes:

- A signed-in user uploads an existing audio file.
- The backend creates a user-owned memo.
- The memo enters the existing transcription lifecycle.
- Completed imported transcripts are searchable.
- Timestamp search results still seek playback.
- The user can rename the memo.

Key constraint:

- Audio import is not the same as Safari in-browser recording support.
  It gives users a practical path for outside recordings without requiring a broader recording engine redesign.

## Slice 2B: Library Organization

After users can import and rename recordings, improve library clarity.

Possible outcomes:

- Suggested titles after transcription completes.
- User can keep, adopt, or edit suggested titles.
- One lightweight organization primitive is selected after feedback: tags, folders, or collections.

Key constraint:

- Do not add all organization models at once.
  Choose the smallest one that real imported memo use justifies.

## Slice 2C: Generated Notes

After capture and organization are usable, add higher-level note generation.

Possible outcomes:

- Summaries.
- Key points.
- Action items.
- Generated document view.
- Editing generated documentation.
- Export transcripts or notes.

Key constraint:

- Decide generation strategy before implementation: local heuristic, local model, hosted model, or adapter-backed provider.
  Keep provider details behind an integration boundary.

## Slice 2D: Reach And Integrations

After the core loop proves useful, expand access and connected workflows.

Possible outcomes:

- Safari and broader browser audio format support.
- Social login.
- Calendar or learning-tool integrations.

Key constraint:

- These should follow evidence that acquisition, browser reach, or connected workflow friction is blocking use.
