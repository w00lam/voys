# Standards for Session Authentication

The following standards apply to this work.

---

## backend/spring

Use `agent-os/standards/backend/spring.md`.

Key points:

- Use Spring Security session authentication for the MVP.
- Hash passwords with a strong Spring Security-supported encoder.
- Organize identity code under `com.voys.identity`.
- Keep controllers, application services, domain, and infrastructure separate.
- Do not expose persistence entities from controllers.

---

## api/rest

Use `agent-os/standards/api/rest.md`.

Key points:

- Prefix APIs with `/api`.
- Use explicit request and response DTOs.
- Return `401 Unauthorized` when the user is not authenticated.
- Use session cookies and intentional CSRF/CORS configuration.

---

## frontend/react

Use `agent-os/standards/frontend/react.md`.

Key points:

- Include browser credentials on API calls.
- Do not store session identifiers or JWTs in JavaScript-accessible storage.
- Load the current user before assuming authentication.
- Clear client-side user state after logout.

---

## testing/testing

Use `agent-os/standards/testing/testing.md`.

Key points:

- Test authentication service behavior.
- Keep tests fast.
- Add integration tests for auth flows as persistence test infrastructure matures.
