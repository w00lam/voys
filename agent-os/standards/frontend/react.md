# React Frontend Standards

## Scope

These standards apply to the React + Vite web frontend for Voys.

The frontend is responsible for authentication screens, browser recording, upload progress, transcription status polling, memo browsing, transcript viewing, timestamp-based audio verification, and search interactions.

## Baseline

- Use React with Vite.
- Use TypeScript unless there is a strong reason not to.
- Keep the frontend as a separate app that consumes the Spring Boot REST API.
- Use browser session cookies for authentication, not localStorage JWTs.
- Use `audio/webm;codecs=opus` for MVP browser recordings.
- Target Chrome and Edge first for recording support.

## Project Structure

Organize by feature, with shared utilities kept small.

```text
src
  app
  features
    auth
    recorder
    memos
    transcript
    search
  shared
    api
    components
    hooks
    types
```

Guidelines:

- Keep route-level screens thin.
- Put feature-specific components, hooks, and API calls under the relevant feature.
- Put reusable UI primitives under `shared/components`.
- Put shared API client behavior under `shared/api`.

## API Calls

- Use one shared API client wrapper.
- Include credentials for authenticated requests.
- Do not store session identifiers or tokens in JavaScript-accessible storage.
- Handle `401 Unauthorized` by sending the user to login or showing an auth-expired state.
- Handle `403 Forbidden` as an authorization failure, not as a missing resource.
- Surface user-safe API error messages from the standard error envelope.

Example fetch option:

```ts
fetch("/api/memos", {
  credentials: "include",
});
```

## Authentication UI

- Provide sign-up, login, logout, and current-user loading flows.
- Avoid assuming the user is authenticated until `/api/me` or an equivalent endpoint confirms it.
- Clear client-side user state after logout.
- Do not log passwords, cookies, CSRF tokens, or auth headers.

## Recording UI

- Use the MediaRecorder API for MVP recording.
- Check support for `audio/webm;codecs=opus` before recording starts.
- Make recording states explicit:
  - `idle`
  - `recording`
  - `paused` if pause is supported
  - `stopped`
  - `uploading`
  - `uploaded`
  - `failed`
- Enforce the 2-hour maximum recording limit in the UI and expect the backend to enforce it again.
- Show elapsed recording time.
- Prevent accidental navigation loss during active recording when practical.
- Save completed recordings with the backend-generated temporary title.

## Uploads

- Upload completed recordings after recording stops.
- Show upload progress when available.
- Let the backend create the memo and store the audio metadata.
- Do not start transcription directly from the frontend; upload completion should trigger or enable backend transcription workflow.
- Show clear retry options for upload failures when retry is safe.

## Transcription Status

- Treat transcription as asynchronous.
- Poll status using a bounded interval until `COMPLETED` or `FAILED`.
- Stop polling when the user leaves the relevant page or when the final status is reached.
- Show user-visible states for `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED`.
- Do not hide the memo if transcription fails; the original recording should remain accessible.

## Transcript And Audio Verification

- Display read-only transcript text in the MVP.
- Render transcript segments when timestamp data is available.
- Search results that point to transcript segments should navigate to that segment.
- The memo detail view should be able to play the audio from a selected segment timestamp.
- Keep full transcript editing out of the MVP.

## Search UI

- Search memo titles and transcript text through backend APIs.
- Debounce free-text search input.
- Show enough context in each result to explain why it matched.
- Include timestamp context for transcript matches when available.
- Preserve query state in the URL when it helps navigation and sharing inside the app.

## State Management

- Prefer local component state and feature hooks until shared state is clearly needed.
- Use a server-state library only if API caching, invalidation, or polling becomes complex enough to justify it.
- Keep recording state explicit and isolated from unrelated app state.

## Accessibility

- Recording controls must be keyboard accessible.
- Buttons must have clear labels.
- Status changes such as recording, uploading, and transcription completion should be visible in text, not only color.
- Audio controls should remain reachable from the keyboard.

## Testing

- Unit test pure utilities and state transitions.
- Component test critical auth, recording, status, and search UI flows when tooling is available.
- Mock MediaRecorder in tests rather than relying on a real browser recording session.

