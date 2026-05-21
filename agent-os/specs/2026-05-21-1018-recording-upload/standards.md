# Standards for Recording Upload

The following standards apply to this work.

---

## backend/spring

Use `agent-os/standards/backend/spring.md`.

Key points:

- Keep memo code under `com.voys.memo`.
- Keep storage behind `StoragePort`.
- Store raw audio files on the filesystem, not in PostgreSQL.
- Authorize by authenticated owner, not client-provided user id.
- Keep controllers focused on HTTP concerns.

---

## api/rest

Use `agent-os/standards/api/rest.md`.

Key points:

- Use `/api` prefix.
- Use `POST /api/memos/recordings` for recording upload.
- Validate content type, size, authenticated owner, and workflow state.
- Do not run Whisper in the upload request.

---

## frontend/react

Use `agent-os/standards/frontend/react.md`.

Key points:

- Use MediaRecorder for browser recording.
- Check `audio/webm;codecs=opus` support.
- Upload completed recordings after recording stops.
- Include browser credentials and CSRF header.
- Keep recording state explicit.

---

## testing/testing

Use `agent-os/standards/testing/testing.md`.

Key points:

- Test upload validation and storage behavior.
- Do not require real microphone access in automated tests.
- Do not require Whisper in normal test suites.
