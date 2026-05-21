# Background Transcription TDD - Shaping Notes

## Scope

This is a test-first handoff branch. It does not implement the background runner. It defines the intended contract.

## Decisions

- Keep `POST /api/memos/{memoId}/transcription` as the command endpoint.
- The command should return after the memo is marked `PROCESSING`.
- The slow Whisper call should happen inside a background job submitted to a dedicated runner.
- The job should load the audio, invoke `TranscriptionPort`, persist transcript text, and update status.
- The job should persist `FAILED` when transcription fails.
- Duplicate starts while `PROCESSING` should be rejected.

## Expected Production Shape

The tests assume an application-layer runner boundary similar to:

```java
public interface TranscriptionJobRunner {
    void submit(Runnable job);
}
```

Antigravity may choose the exact implementation, but the observable behavior in the tests should stay intact.

## Out Of Scope

- Real queue infrastructure.
- Scheduled retry.
- Segment parsing.
- UI polling changes.
- Actual Whisper process execution in tests.
