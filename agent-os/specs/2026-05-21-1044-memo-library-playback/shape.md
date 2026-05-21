# Memo Library Playback - Shaping Notes

## Scope

This spec makes saved recordings visible and playable. It does not implement transcript display, search, or Whisper execution.

## Decisions

- Memo queries are scoped by authenticated principal.
- Audio playback uses the backend as an owner-checking gateway.
- The frontend fetches audio as a credentialed blob rather than exposing local storage paths.
- The first playback endpoint returns the complete resource; byte-range streaming can be added later when needed.

## Out Of Scope

- Transcript APIs.
- Search.
- Range requests.
- Waveforms or waveform scrubbing.
- Download/export controls.

## Risks

- Large audio playback will eventually need HTTP range support.
- Blob URLs should be revoked when the selected memo changes.
- File-not-found should return a safe API error, not a filesystem path.
