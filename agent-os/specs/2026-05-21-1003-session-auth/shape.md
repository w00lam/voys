# Session Authentication - Shaping Notes

## Scope

This spec implements the first practical authentication workflow for the web MVP. It protects future memo, recording, transcription, and search APIs by establishing user identity through server-side sessions.

## Decisions

- Use Spring Security session authentication, not JWT.
- Use email/password for MVP authentication.
- Store password hashes with the configured Spring `PasswordEncoder`.
- Use a CSRF token endpoint for the React client because mutating session-authenticated requests need CSRF protection.
- Use explicit API DTOs and keep JPA entities internal.
- Keep the UI intentionally simple until recording and memo workflows exist.

## Context

- Product requires personal recordings and transcripts to be user-scoped.
- Architecture already accepts simple authentication in the MVP.
- Frontend calls must include browser credentials and must not store session tokens in JavaScript storage.

## Out Of Scope

- JWT issuance.
- OAuth/social login.
- Email verification.
- Password reset.
- Account deletion.
- Role-based administration.

## API Sketch

```text
POST /api/auth/signup
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/csrf
GET  /api/me
```

## Risks

- CSRF configuration must support the Vite dev origin without weakening session protection.
- Duplicate email handling should return a safe error without exposing internals.
- Future user-owned resources must use the authenticated principal, not client-provided user IDs.
