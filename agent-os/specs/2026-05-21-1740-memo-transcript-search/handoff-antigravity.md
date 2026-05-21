# Antigravity Handoff: Memo And Transcript Search

## Branch

`feature/25-memo-transcript-search`

## Issue

https://github.com/w00lam/voys/issues/25

## Your Role

Implement production backend code so the tests in this branch pass.

Do not remove or weaken the tests. Adjust tests only if the same user-facing contract remains protected and the implementation reveals a better name or boundary.

## Commands

From `backend/`:

```powershell
$env:JAVA_HOME='C:\Users\woo_lam\.jdks\corretto-21.0.9'
.\gradlew.bat test --tests com.voys.search.application.SearchServiceTests --tests com.voys.search.api.SearchControllerTests
.\gradlew.bat test
```

## Expected Failing Tests At Handoff

- `backend/src/test/java/com/voys/search/application/SearchServiceTests.java`
- `backend/src/test/java/com/voys/search/api/SearchControllerTests.java`

They fail because the search package does not exist yet.

## Implementation Requirements

- Add `GET /api/search?q={query}`.
- Use `@AuthenticationPrincipal UserPrincipal` and pass `principal.id()` to the application service.
- Add a `SearchService` that trims and lowercases queries.
- Reject blank queries with `InvalidSearchQueryException`.
- Add a repository/adapter that searches memo titles and transcript text for the authenticated owner only.
- Return `SearchResult` with:
  - `memoId`
  - `title`
  - `matchType`
  - `snippet`
  - `transcriptionStatus`
- Use match types `TITLE` and `TRANSCRIPT`.
- Use a default limit of 20.

## Notes

- Keep snippets simple in this slice.
- Do not add frontend UI yet.
- Do not add transcript timestamp behavior yet.
