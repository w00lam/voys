# MVP Verification Checklist

Use this checklist before calling the Phase 1 MVP ready for a demo or broader manual testing.

## Local Setup

- PostgreSQL is running through Docker Compose.
- Backend starts without errors from `backend/`.
- Frontend starts without errors from `frontend/`.
- Frontend can reach backend APIs through the Vite `/api` proxy.
- Local storage directories for audio and transcript output are outside Git.
- Whisper CLI command is configured for the local machine.
- The configured Whisper model is known before testing, especially when comparing speed and quality.
- Docker users know the first build and first transcription can be slow because images, dependencies, and Whisper models may need to download.

## Automated Checks

- Backend tests pass.
- Frontend tests pass.
- Frontend production build passes.
- GitHub Actions checks pass on the PR.

Recommended commands:

```powershell
cd backend
.\gradlew.bat test
```

```powershell
cd frontend
cmd /c npm test
cmd /c npm run build
```

## Happy Path

- A new user can sign up with email, display name, and password.
- The signed-in user can log out and log back in.
- The browser recording control is visible only after authentication.
- Chrome or Edge can start a WebM/Opus recording.
- The recording timer increments while recording.
- The recording UI shows the 2-hour maximum.
- Stopping a recording uploads it successfully.
- A saved memo appears in the memo list with a temporary title.
- The uploaded memo is user-owned and visible only to the signed-in user.
- The memo detail view loads the original audio.
- Starting transcription changes the memo transcript status to `PROCESSING`.
- Transcription continues in the background instead of blocking the HTTP request.
- Polling updates the transcript status until `COMPLETED` or `FAILED`.
- For a longer recording, `PROCESSING` remains visible and the memo/audio stay usable while transcription runs.
- A completed transcript displays the full read-only transcript text.
- Timestamped transcript segments display in position order.
- Clicking a transcript segment seeks the audio to that timestamp without autoplay.
- Search finds matching memo titles.
- Search finds matching completed transcript text.
- Transcript search results include a timestamp when a matching segment exists.
- Clicking a timestamped search result opens the memo and seeks audio to the matching timestamp.

## Boundaries And Failure Cases

- Anonymous users cannot access memo, audio, transcript, or search APIs.
- User A cannot access User B's memo detail, audio, transcript, or search results.
- Empty recording uploads are rejected.
- Non-WebM recordings are rejected.
- Recordings larger than the configured upload size are rejected.
- Provided recording durations less than or equal to zero are rejected.
- Provided recording durations greater than 2 hours are rejected.
- Browser recording stops automatically at the 2-hour limit.
- Duplicate transcription starts are rejected while a memo is already processing.
- Whisper failures mark the memo transcription as `FAILED` without deleting the original audio.
- Whisper failures show a safe user-understandable reason and practical next step when the backend can classify the cause.
- Whisper failures do not expose local filesystem paths, stack traces, or raw command output in the UI.
- Search rejects blank queries.
- Transcript and search views do not expose local filesystem paths.

## Regression Watchlist

- Session cookie and CSRF behavior still work after frontend refresh.
- Audio object URLs are revoked when switching selected memos.
- Transcription polling stops after terminal statuses.
- Search debounce does not call the backend for blank input.
- Timestamp seek changes `currentTime` but does not call `play()`.
- Full transcript text remains read-only in the MVP.

## Demo Readiness

- Prepare at least one short test recording.
- Prepare one recording with a known searchable phrase.
- Confirm Whisper is available before the demo.
- Confirm the selected Whisper model is appropriate for the demo goal: `tiny` for speed, `base` or `small` for better quality.
- Run one short transcription before a demo when possible so Docker images, dependencies, and the selected Whisper model are already cached.
- For Docker demos, tell testers that the first build, first model download, and first transcription may take noticeably longer than later runs.
- Confirm local storage has enough disk space for audio and transcript output.
- Confirm backend and frontend logs do not show unexpected errors during the happy path.

## Early Feedback Notes

After a Phase 1 MVP demo, capture:

- Whether users understood why transcription was pending, completed, or failed.
- Whether transcript quality was acceptable for their audio and language mix.
- Whether search results helped them jump back to useful audio moments.
- Which Phase 2 candidates would make the MVP useful enough for repeated personal use.
- Any setup confusion around Docker, Whisper model choice, first transcription time, browser support, or long recordings.
