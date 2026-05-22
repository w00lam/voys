# Shape: Recording Duration Limit

## Current Problem

The architecture and frontend standards define a 2-hour MVP recording limit, but the frontend does not stop recording at that boundary and the backend does not validate `durationSeconds`.

## Desired Flow

1. User starts browser recording.
2. UI shows elapsed time and communicates the 2-hour maximum.
3. When elapsed time reaches 2 hours, the recorder is stopped automatically.
4. The existing stop handler uploads the recording with `durationSeconds = 7200`.
5. If a client submits `durationSeconds > 7200`, the backend rejects the upload with `recording.invalid`.
6. If a client submits `durationSeconds <= 0`, the backend rejects the upload.

## Contract

Maximum duration:

```text
7200 seconds
```

Backend behavior:

```text
durationSeconds = null   -> accepted for backward compatibility
durationSeconds = 1      -> accepted
durationSeconds = 7200   -> accepted
durationSeconds = 7201   -> rejected
durationSeconds = 0      -> rejected
durationSeconds = -1     -> rejected
```

Frontend behavior:

```text
elapsed < 7200 seconds   -> recording continues
elapsed >= 7200 seconds  -> MediaRecorder.stop() is called once
```

## Implementation Direction

- Put the maximum duration in a named constant on the frontend.
- Put the maximum duration in backend validation code, preferably configurable with a default of 7200 seconds.
- Avoid duplicating stop calls if the user manually stops near the limit.
- Keep the existing upload flow responsible for saving the completed recording.
