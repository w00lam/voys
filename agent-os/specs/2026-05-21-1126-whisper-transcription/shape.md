# Whisper Transcription - Shaping Notes

## Scope

This is the first transcription slice. It wires the domain state, transcript persistence, adapter boundary, and UI control. It does not introduce a background queue yet.

## Decisions

- Keep `TranscriptionPort` as the integration boundary.
- Start with plain text transcript storage.
- Keep transcript segments and timestamp parsing out of this slice.
- Run transcription only through an explicit user command for now.
- Persist `PROCESSING`, `COMPLETED`, and `FAILED` status transitions.
- Treat a failed transcription as recoverable; the memo and audio remain available.

## Out Of Scope

- Job queue or worker process.
- Segment timestamps.
- Search indexing.
- Speaker labels.
- Summaries.

## Risks

- A synchronous command can block for long recordings. This is acceptable only as a temporary foundation and should move to a worker soon.
- Whisper CLI installation differs by machine. The command and output directory must be configurable.
