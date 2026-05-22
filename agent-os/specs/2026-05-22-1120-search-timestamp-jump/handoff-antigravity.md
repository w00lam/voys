# Antigravity Handoff: Search Timestamp Jump

## Branch

`feature/33-search-timestamp-jump`

## Issue

https://github.com/w00lam/voys/issues/33

## Your Role

Implement production code so the tests in this branch pass.

Do not remove or weaken the tests. Adjust tests only if the same user-facing contract remains protected.

## Commands

From `backend/`:

```powershell
.\gradlew.bat test
```

From `frontend/`:

```powershell
cmd /c npm test
cmd /c npm run build
```

## Expected Failing Tests At Handoff

- `backend/src/test/java/com/voys/search/infrastructure/persistence/JpaSearchRepositoryTests.java`
- `backend/src/test/java/com/voys/search/application/SearchServiceTests.java`
- `backend/src/test/java/com/voys/search/api/SearchControllerTests.java`
- `frontend/src/App.test.tsx`

They fail because search results do not yet expose `segmentStartSeconds`, and the frontend does not seek audio from timestamped search results.

## Implementation Requirements

- Add nullable `segmentStartSeconds` to the search result API contract.
- Return segment start time for transcript segment matches.
- Keep title matches returning `segmentStartSeconds: null`.
- Render transcript result timestamps in `mm:ss` format.
- Seek the selected audio element to the timestamp after the memo loads.
- Do not auto-play audio.

## Notes

- A transcript without segments may still use the old transcript text fallback and return no timestamp.
- Transcript segment list UI is not part of this slice.
