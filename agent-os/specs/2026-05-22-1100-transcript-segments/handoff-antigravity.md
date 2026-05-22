# Antigravity Handoff: Transcript Segments

## Branch

`feature/31-transcript-segments`

## Issue

https://github.com/w00lam/voys/issues/31

## Your Role

Implement production backend code so the tests in this branch pass.

Do not remove or weaken the tests. Adjust tests only if the same product contract remains protected.

## Commands

From `backend/`:

```powershell
.\gradlew.bat test
```

## Expected Failing Tests At Handoff

- `backend/src/test/java/com/voys/transcription/application/TranscriptionWorkflowServiceSegmentTests.java`

They fail because production code does not yet have:

- `TranscriptionPort.TranscriptionSegment`
- `TranscriptionResult(String text, List<TranscriptionSegment> segments)`
- `TranscriptSegment`
- `TranscriptSegmentRepository`
- workflow persistence for timestamped segments

## Implementation Requirements

- Extend the transcription port result to include ordered timestamp segments.
- Update existing fake transcription adapters/tests to use the new result contract.
- Add a `TranscriptSegment` entity linked to `Transcript`.
- Add a repository method to delete old segments for a transcript before saving regenerated segments.
- Save returned segments in their original order with zero-based positions.
- Keep memo completion/failure behavior unchanged.
- Update `LocalWhisperAdapter` so it can return segment timestamps from Whisper output without requiring a real Whisper run in normal unit tests.

## Notes

- Search timestamp display and audio seek are not part of this slice.
- Keep the implementation small and backend-only.
