# Git Branching Standards

## Scope

These standards define how Voys uses Git branches, commits, and pull requests.

## Branch Model

Use a simple trunk-based workflow:

- `main` is the stable integration branch.
- Work happens on short-lived branches.
- Branches are merged back to `main` through pull requests when possible.
- Keep branches small enough to review and test.

Avoid long-running release, develop, or environment branches unless the project grows enough to need them.

## Branch Names

Use descriptive branch prefixes:

- `feature/*` for user-facing product features.
- `fix/*` for bug fixes.
- `chore/*` for setup, tooling, dependency, and infrastructure work.
- `docs/*` for documentation-only changes.
- `refactor/*` for behavior-preserving code restructuring.
- `test/*` for test-only improvements.

Examples:

```text
chore/project-bootstrap
feature/session-auth
feature/browser-recording
feature/whisper-transcription
fix/upload-content-type-validation
docs/architecture-decisions
```

## Commit Guidelines

- Commit focused, coherent changes.
- Prefer clear imperative commit messages.
- Keep generated build output, local storage files, secrets, and audio files out of commits.
- Do not mix unrelated product changes and tooling changes in the same commit when they can be separated.

Examples:

```text
Add Agent OS product architecture
Bootstrap Spring Boot and React projects
Add session authentication foundation
Implement browser recording upload
```

## Pull Request Guidelines

Each pull request should include:

- What changed.
- Why it changed.
- How it was tested.
- Any known follow-up work.

Keep PRs aligned with Agent OS specs when a spec exists. Reference the relevant spec folder in the PR description.

Use the repository PR template for all pull requests. CI should run automatically on PRs targeting `main`.

CODEOWNERS should request `@w00lam` for review by default. Review and merge decisions remain manual.

Working branch pushes should automatically open or update a draft PR against `main` through GitHub Actions. The bot should also add an automated review checklist comment inside the PR.

The same workflow should create or reuse a tracking issue for the branch and link it from the PR with `Closes #issue`. This keeps planning, review, and merge history connected.

Automatic PR creation applies to:

- `feature/**`
- `fix/**`
- `chore/**`
- `docs/**`
- `refactor/**`
- `test/**`

The bot review is a guardrail, not approval. It can point out missing specs, risky files, workflow changes, and validation gaps, but the final review and merge decision belongs to `@w00lam`.

Automated review comments should use branch-specific titles like:

```text
Review: chore/project-bootstrap
Review: feature/session-auth
```

## Main Branch Rules

- `main` should remain runnable.
- Do not commit directly to `main` once active implementation begins.
- Do not force-push `main`.
- Do not auto-merge PRs.
- Let CI provide automated verification, but keep the final merge decision manual.
- Prefer squash or regular merge commits based on GitHub repo settings, but keep history understandable.

## Current Bootstrap Flow

The initial project bootstrap work should use:

```text
chore/project-bootstrap
```

This branch includes Agent OS product docs, standards, first bootstrap spec, Spring Boot backend skeleton, React frontend skeleton, and local PostgreSQL setup.
