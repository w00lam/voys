# Antigravity Handoff: Search UI

## Branch

`feature/27-search-ui`

## Issue

https://github.com/w00lam/voys/issues/27

## Your Role

Implement production frontend code so the tests in this branch pass.

Do not remove or weaken the tests. Adjust tests only if the same user-facing contract remains protected.

## Commands

From `frontend/`:

```powershell
cmd /c npm test
cmd /c npm run build
```

## Expected Failing Tests At Handoff

- `frontend/src/App.test.tsx`

They fail because the search UI does not exist yet and because the memo metadata separator should be rendered as `·`.

## Implementation Requirements

- Add an authenticated search UI.
- Add a frontend API function for `GET /api/search?q={query}`.
- Debounce search input before calling the API.
- Do not call the API for blank input.
- Render title, match type, snippet, and transcription status.
- Open the selected memo when a search result is clicked.
- Preserve existing recording, memo list, playback, transcription polling, and transcript display behavior.
- Fix any memo metadata mojibake so the separator renders as `·`.

## Notes

- Timestamp jump and audio seek are not part of this slice.
- Keep the implementation small and consistent with the current `App.tsx` structure.
