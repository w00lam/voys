# Phase 2 Candidate Comparison

Use this comparison after Phase 1.5 stabilization to choose the first post-launch increment.
The goal is to improve repeated personal use without making the product depend on a large new platform bet too early.

## Decision Criteria

- Repeated-use value: helps users come back to Voys with real recordings.
- Reuse of MVP foundations: builds on recording, upload, transcription, search, and memo detail flows.
- Risk and dependency level: avoids major third-party, browser, or AI-generation uncertainty in the first Phase 2 slice.
- Reviewability: can be shipped as a focused Agent OS spec with clear acceptance criteria.

## Candidates

| Candidate | User Value | Fit After Phase 1.5 | Main Risk | Recommendation |
| --- | --- | --- | --- | --- |
| Audio file upload for recordings captured outside the app | High. Users can bring meetings, lectures, interviews, and study audio they already have. | Strong. Reuses memo creation, storage, transcription, status polling, transcript display, search, and playback. | File type validation, upload size limits, duration metadata, and browser playback compatibility. | Select for Phase 2A. |
| Manual memo title editing | High. Users need recognizable memo names for repeated use and uploaded files. | Strong. Small API/UI addition around existing memo metadata. | Ownership and validation edge cases. | Include in Phase 2A as a small companion capability. |
| Suggested titles after transcription | Medium to high. Reduces cleanup effort after transcription completes. | Medium. Needs a generation strategy and fallback behavior. | Requires deciding whether to use heuristics, local models, or a hosted LLM. | Defer to Phase 2B after manual title editing exists. |
| Tags, folders, or collections | Medium to high for larger libraries. | Medium. Benefits from more real memo volume and title editing first. | Product shape may split between tags, folders, and collections too early. | Defer until users have enough imported memos to validate organization needs. |
| Safari and broader browser recording support | Medium. Expands browser coverage. | Medium. Recording compatibility is separate from core memo value. | MediaRecorder format differences and backend transcoding decisions. | Defer; audio file upload gives Safari users a workaround sooner. |
| Speaker labels or transcript segmentation improvements | Medium. Makes long conversations easier to scan. | Medium. Builds on transcript segments. | Transcription tooling limitations and uncertain quality. | Defer until transcript quality feedback is clearer. |
| Summaries, key points, and action items | High. Moves Voys toward generated notes. | Medium. Needs completed transcript foundation. | Model/provider choice, safety, cost, latency, and editing workflow. | Defer to a later Phase 2 slice after import/title basics. |
| Generated document view and editing | High for review workflows. | Low to medium. Depends on summarization and editing model. | Larger UX and persistence surface. | Defer until generated note strategy is chosen. |
| Export transcripts or notes | Medium. Helps users move data elsewhere. | Medium. Transcript export can be simple; note export depends on generated notes. | Format decisions and permission checks. | Defer; consider transcript export after audio import. |
| Social login | Medium. Reduces signup friction. | Low for core value. | OAuth configuration, account linking, and security review. | Defer until acquisition/onboarding becomes the bottleneck. |
| Calendar or learning-tool integrations | Medium to high later. | Low now. | Third-party auth, sync semantics, and privacy boundaries. | Defer until core capture/review loop is stronger. |

## Phase 2A Selection

Phase 2A should focus on:

- Uploading existing audio files into the memo library.
- Reusing the current transcription, status, transcript, search, and playback flow for imported audio.
- Letting users rename memo titles so imported recordings can be identified and organized.

This keeps Phase 2A close to the MVP architecture while unlocking real recordings captured outside Chrome/Edge.

## Deferred Follow-Ups

- Phase 2B candidate: suggested titles after transcription, building on manual title editing.
- Phase 2C candidate: lightweight organization with tags or collections after imported memo volume grows.
- Later Phase 2 candidate: summaries, action items, generated document view, editing, and export.
