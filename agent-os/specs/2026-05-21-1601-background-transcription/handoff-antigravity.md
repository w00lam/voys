# Antigravity Handoff

## Branch

```text
test/17-background-transcription
```

## Mission

Implement production code to satisfy the failing unit tests added by Codex.

Do not weaken or remove the tests. Treat the tests as the specification.

## First Commands

```text
git status
backend/gradlew.bat test --tests com.voys.transcription.application.TranscriptionWorkflowServiceBackgroundTests
```

The test suite is expected to fail at handoff time because the background runner boundary and duplicate-start exception do not exist yet.

## Expected Implementation Direction

Add an application-layer runner boundary similar to:

```java
public interface TranscriptionJobRunner {
    void submit(Runnable job);
}
```

Provide a simple Spring implementation for now. It can run jobs through a single-thread executor or Spring async infrastructure, but tests only require the boundary and observable behavior.

Update `TranscriptionWorkflowService` so:

- `startTranscription(ownerId, memoId)` validates ownership.
- If the memo is already `PROCESSING`, it throws `TranscriptionAlreadyRunningException`.
- It marks the memo `PROCESSING`.
- It submits exactly one background job.
- It returns a `TranscriptionResponse` immediately with status `PROCESSING`.
- The queued job loads audio, calls `TranscriptionPort`, saves transcript text, and marks `COMPLETED`.
- If the queued job fails, it marks `FAILED` and does not save transcript text.

## Guardrails

- Do not call Whisper inline inside the request path.
- Do not keep a database transaction open while `TranscriptionPort.transcribe(...)` runs.
- Do not accept user ids from the client.
- Do not run a real Whisper process in unit tests.

## Completion Criteria

```text
backend/gradlew.bat test
cmd /c npm run build
```

After implementation passes, create or update the PR from a normal feature branch.
