# Session Authentication Plan

## Goal

Implement the first usable email/password authentication slice for Voys with Spring Security server-side sessions and a React client that can sign up, log in, log out, and load the current user.

## Task 1: Save Spec Documentation

Create `agent-os/specs/2026-05-21-1003-session-auth/` with:

- `plan.md` - implementation plan.
- `shape.md` - shaping notes and decisions.
- `standards.md` - applicable standards.
- `references.md` - project context and references.

## Task 2: Add Identity Persistence

Add a minimal user account model:

- Stable UUID primary key.
- Unique normalized email.
- Display name.
- BCrypt password hash.
- Created and updated timestamps.

Keep persistence entities out of API responses.

## Task 3: Add Session Auth APIs

Implement:

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/me`
- `GET /api/auth/csrf`

Use Spring Security sessions and avoid JWT for this MVP slice.

## Task 4: Add Frontend Auth Flow

Add React auth UI and API calls:

- Load current user on app start.
- Show sign-up and login forms when unauthenticated.
- Show current user and logout action when authenticated.
- Include browser credentials and CSRF token header for mutating requests.

## Task 5: Add Tests And Verification

Add focused backend tests for identity service behavior and keep existing frontend/backend builds passing.

## Acceptance Criteria

- A user can sign up with email, password, and display name.
- A signed-up user is authenticated with a server session.
- A user can log in with email/password.
- `GET /api/me` returns the current authenticated user and returns `401` when unauthenticated.
- A user can log out and clear the authenticated session.
- Frontend auth requests include credentials and CSRF headers.
- No JWTs, secrets, or local credentials are stored in frontend storage.
