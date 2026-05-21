# Recording Upload - Shaping Notes

## Scope

This spec implements the first useful recording workflow after authentication. The user can record in the browser and save the recording as a voice memo. The saved memo is not transcribed yet.

## Decisions

- Accept only WebM audio for the first browser recording slice.
- Store raw audio on the local filesystem through `StoragePort`.
- Store memo and audio metadata in PostgreSQL through JPA.
- Generate temporary titles on the backend so the user can save without naming the memo.
- Keep transcription status as `PENDING` after upload, but do not start Whisper in this spec.
- Use the authenticated principal as the owner; never accept a user id from the client.

## Out Of Scope

- Whisper execution.
- Transcript persistence.
- Search.
- Audio playback from stored files.
- Audio editing.
- Upload retry/resume.

## API Sketch

```text
POST /api/memos/recordings
multipart:
  audio: WebM audio file
  durationSeconds?: number
```

## Risks

- Browser `MediaRecorder` content types can vary; the MVP should be strict but not brittle.
- Large recordings need backend size limits; the first limit should be configurable.
- File paths must be generated server-side to prevent path traversal.
