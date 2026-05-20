# Testing Standards

## Scope

These standards apply to backend, frontend, storage, transcription, and integration testing for Voys.

## Testing Philosophy

- Test behavior that protects product workflows.
- Keep tests close to the layer they verify.
- Prefer fast unit tests for business rules and state transitions.
- Use integration tests for persistence, security, and important API flows.
- Avoid requiring real long audio files or real Whisper runs in normal test suites.

## Backend Unit Tests

Use unit tests for:

- Domain rules and state transitions.
- Application service branching logic.
- Ownership and authorization decisions that do not require Spring Security integration.
- Temporary title generation.
- Recording duration and status transition rules.
- Transcription result parsing when it can be tested with small fixtures.

Guidelines:

- Use fake ports for storage and transcription.
- Do not touch the filesystem unless the test is specifically for a storage adapter.
- Do not invoke Whisper CLI in unit tests.

## Backend Integration Tests

Use integration tests for:

- Authentication flows: sign-up, login, logout, current user.
- Session-protected API access.
- CSRF/CORS behavior where relevant.
- Memo creation and ownership isolation.
- Upload validation and metadata persistence.
- Search behavior against persisted transcripts.

Guidelines:

- Prefer test database isolation.
- Keep test fixtures small.
- Verify user A cannot access user B resources.

## Adapter Tests

Storage adapter tests should verify:

- Files are written under the configured storage root.
- Paths cannot escape the storage root.
- Metadata and stored paths stay consistent.
- Cleanup behavior is explicit.

Whisper adapter tests should verify:

- CLI command construction.
- Timeout and failure handling.
- Parsing of representative Whisper output fixtures.
- No test should require a large model download by default.

## Frontend Tests

Use frontend tests for:

- Auth UI states.
- Recording state transitions.
- Upload progress and failure states.
- Transcription status polling behavior.
- Search result rendering and timestamp navigation.

Guidelines:

- Mock API calls.
- Mock MediaRecorder.
- Do not depend on real microphone access in automated tests.

## Manual Verification

Before considering the MVP recording flow complete, manually verify:

- Sign-up and login.
- Start and stop recording in Chrome or Edge.
- Save recording with temporary title.
- Upload completes.
- Transcription status updates.
- Transcript appears.
- Search finds transcript text.
- Timestamp result opens the memo and can play near the matching audio segment.

