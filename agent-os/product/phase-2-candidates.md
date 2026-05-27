# Phase 2 Scope And Candidate Comparison

Use this comparison after Phase 1.5 stabilization to choose the first post-launch increment.
The goal is to define Phase 2 as a whole, then split it into implementation slices that can be designed, tested, reviewed, and merged independently.

## Decision Criteria

- Repeated-use value: helps users come back to Voys with real recordings.
- Reuse of MVP foundations: builds on recording, upload, transcription, search, and memo detail flows.
- Risk and dependency level: avoids major third-party, browser, or AI-generation uncertainty in the first Phase 2 slice.
- Reviewability: can be shipped as a focused Agent OS spec with clear acceptance criteria.

## Phase 2 Direction

Phase 2 should improve the core capture -> transcribe -> search -> verify loop before expanding into integrations.
The phase should move in this order:

1. Capture more real recordings.
2. Make the library understandable as it grows.
3. Generate higher-level notes from completed transcripts.
4. Expand reach through browser support, login options, and integrations when the core loop is strong.

## Candidates

| Candidate | User Value | Fit After Phase 1.5 | Main Risk | Recommendation |
| --- | --- | --- | --- | --- |
| Audio file upload for recordings captured outside the app | High. Users can bring meetings, lectures, interviews, and study audio they already have. | Strong. Reuses memo creation, storage, transcription, status polling, transcript display, search, and playback. | File type validation, upload size limits, duration metadata, and browser playback compatibility. | Phase 2A. |
| Manual memo title editing | High. Users need recognizable memo names for repeated use and uploaded files. | Strong. Small API/UI addition around existing memo metadata. | Ownership and validation edge cases. | Phase 2A. |
| Suggested titles after transcription | Medium to high. Reduces cleanup effort after transcription completes. | Medium. Needs a generation strategy and fallback behavior. | Requires deciding whether to use heuristics, local models, or a hosted LLM. | Phase 2B. |
| Tags, folders, or collections | Medium to high for larger libraries. | Medium. Benefits from more real memo volume and title editing first. | Product shape may split between tags, folders, and collections too early. | Phase 2B after usage feedback. |
| Safari and broader browser recording support | Medium. Expands browser coverage. | Medium. Recording compatibility is separate from core memo value. | MediaRecorder format differences and backend transcoding decisions. | Phase 2D unless browser reach blocks usage earlier. |
| Speaker labels or transcript segmentation improvements | Medium. Makes long conversations easier to scan. | Medium. Builds on transcript segments. | Transcription tooling limitations and uncertain quality. | Later Phase 2 candidate after transcript quality feedback. |
| Summaries, key points, and action items | High. Moves Voys toward generated notes. | Medium. Needs completed transcript foundation. | Model/provider choice, safety, cost, latency, and editing workflow. | Phase 2C. |
| Generated document view and editing | High for review workflows. | Low to medium. Depends on summarization and editing model. | Larger UX and persistence surface. | Phase 2C after generation strategy. |
| Export transcripts or notes | Medium. Helps users move data elsewhere. | Medium. Transcript export can be simple; note export depends on generated notes. | Format decisions and permission checks. | Phase 2C after document shape stabilizes. |
| Social login | Medium. Reduces signup friction. | Low for core value. | OAuth configuration, account linking, and security review. | Phase 2D if onboarding friction is measured. |
| Calendar or learning-tool integrations | Medium to high later. | Low now. | Third-party auth, sync semantics, and privacy boundaries. | Phase 2D after generated notes are valuable. |

## Proposed Phase Breakdown

### Phase 2A: Capture Expansion

- Uploading existing audio files into the memo library.
- Reusing the current transcription, status, transcript, search, and playback flow for imported audio.
- Letting users rename memo titles so imported recordings can be identified and organized.

This keeps Phase 2A close to the MVP architecture while unlocking real recordings captured outside Chrome/Edge.

### Phase 2B: Library Organization

- Suggested titles after transcription.
- A single lightweight organization primitive, chosen from tags, folders, or collections after imported memo usage exists.

### Phase 2C: Generated Notes

- Summaries, key points, and action items.
- Generated document view.
- Editing generated documentation.
- Export transcripts or generated notes.

### Phase 2D: Reach And Integrations

- Safari and broader browser audio format support.
- Social login if onboarding friction becomes the bottleneck.
- Calendar or learning-tool integrations after the generated note loop is useful.
