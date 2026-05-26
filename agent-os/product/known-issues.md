# Known Issues

Use this list when sharing the Phase 1 MVP for local demos or early feedback.

## Transcription Feedback

- Phase 1.5 shows safe, user-understandable transcription failure reasons for common local Whisper failures.
- Failure messages intentionally avoid raw command output, stack traces, local filesystem paths, and full environment details.
- Some unexpected backend or environment failures may still be grouped into a generic unexpected processing message.
- Original audio remains available after transcription failure.

## Whisper Model Quality

- The Docker demo defaults to the `tiny` model to keep local CPU demos manageable.
- `tiny` is fast but can produce inaccurate text, weak punctuation, missing words, or poor results for noisy audio, quiet speakers, domain terms, and Korean/English mixed speech.
- Larger models such as `base` or `small` usually improve quality but increase model download size, first-run time, CPU use, and total transcription time.

## Docker First Run

- `docker compose up --build` can take a long time on the first run because it downloads base images and builds the backend image with Whisper, ffmpeg, and CPU-only Torch dependencies.
- The first transcription for a model can also take longer because Whisper downloads the model into the `whisper-cache` Docker volume.
- Rebuilding after dependency changes can be slow even when the application code is small.

## Long Recordings

- The MVP allows recordings up to 2 hours, but long recordings can remain in `PROCESSING` for several minutes or longer on CPU-only machines.
- Phase 1.5 adds processing copy so users know long recordings, Docker first runs, and first Whisper model downloads can take several minutes.
- There is no detailed progress percentage for transcription yet.
- If the backend container is stopped while a transcription job is running, the job may need to be started again after restart.

## Browser And Audio Boundaries

- The MVP is Chrome/Edge-first and expects WebM/Opus recordings from the browser.
- Safari and externally uploaded audio files are planned for later phases.
- Poor microphone quality, background noise, or very quiet speech can reduce transcript quality.

## Phase 1.5 Follow-Up Candidates

- Add a manual retry path for failed transcriptions.
- Add more detailed worker observability or progress signals if real demos show that `PROCESSING` copy is not enough.
- Collect early user feedback before selecting Phase 2 items.
- Decide whether Whisper model selection should remain environment-only or become visible in a later settings UI.
