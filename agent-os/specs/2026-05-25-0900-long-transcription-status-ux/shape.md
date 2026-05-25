# Shape: Long Transcription Status UX

## Current Problem

The MVP correctly polls transcription status, but a long recording can remain in `PROCESSING` for several minutes.
During Docker first run or first transcription for a Whisper model, users may think the app is stuck because the transcript area only shows a generic empty state.

## Desired Flow

1. User selects a saved memo.
2. Frontend loads memo detail, audio, and transcript status.
3. Transcript status is `PROCESSING`.
4. Transcript panel shows status as processing.
5. Transcript panel also shows practical guidance:
   - Long recordings or first transcriptions can take several minutes.
   - The user can keep the memo selected and continue using the original audio while transcription runs.
6. Existing polling continues until `COMPLETED` or `FAILED`.
7. On `COMPLETED`, the transcript appears as it does today.
8. On `FAILED`, the failure reason UI remains intact.

## Expected Copy

Use concise Korean UI copy. The failing test protects this sentence:

```text
긴 녹음이나 첫 전사는 몇 분 이상 걸릴 수 있습니다.
```

Additional supporting copy may be added if it stays short and does not clutter the transcript panel.

## Implementation Direction

- Keep this local to the selected memo transcript panel.
- Avoid backend changes.
- Keep the existing polling effect and terminal status handling.
- Do not hide transcript segments or text if the backend ever returns partial data while processing.
- Prefer plain text inside the existing transcript panel styling rather than a new large UI surface.
