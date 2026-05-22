# Shape: Transcript Segments

## Current Problem

The transcription workflow saves only the full transcript text. The MVP requires timestamped transcript segments so users can later open a search result near the relevant point in a long recording.

## Desired Flow

1. A transcription job runs for an uploaded memo.
2. `TranscriptionPort` returns:
   - full transcript text,
   - ordered timestamped segments.
3. The workflow saves the full `Transcript`.
4. The workflow replaces any previous segments for that transcript.
5. The workflow saves each returned segment with:
   - transcript reference,
   - zero-based position,
   - start seconds,
   - end seconds,
   - segment text.
6. The memo is marked `COMPLETED`.

## Contract

Expected `TranscriptionPort` result shape:

```java
new TranscriptionResult(
    "full transcript text",
    List.of(
        new TranscriptionSegment(0.0, 4.2, "first segment"),
        new TranscriptionSegment(4.2, 8.0, "second segment")
    )
)
```

Expected persistence shape:

```text
transcripts
  id
  memo_id
  text
  created_at
  updated_at

transcript_segments
  id
  transcript_id
  position
  start_seconds
  end_seconds
  text
  created_at
```

## Implementation Direction

- Add a `TranscriptSegment` JPA entity in the transcription persistence package.
- Add a `TranscriptSegmentRepository`.
- Keep segment persistence inside the post-Whisper transaction.
- Delete old segments for the transcript before saving new ones.
- Store timestamps in seconds as decimal-capable values. `double` is acceptable for this MVP unless the implementation chooses `BigDecimal` consistently behind the same application contract.
