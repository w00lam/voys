# Repository Instructions

Follow the nearest AGENTS.md file before making changes.

This project uses Agent OS. Before product planning, architecture work, or implementation, review:

- `agent-os/product/`
- `agent-os/standards/index.yml`
- relevant files under `agent-os/specs/` when they exist

Keep changes small, incremental, and maintainable. Preserve user changes.

## Codex And Antigravity Collaboration

Use Codex + Antigravity TDD handoff for feature work by default, but do not force it for every change.

Use Antigravity handoff by default when the work:

- adds a new user-facing feature,
- changes a user workflow,
- changes frontend/backend state transitions,
- benefits from failing tests before implementation,
- touches multiple application layers,
- or has meaningful regression risk.

Use Codex direct implementation by default when the work is:

- documentation-only,
- repository cleanup,
- CI/configuration maintenance,
- typo/copy fixes,
- small review follow-up fixes,
- or a narrow bugfix with an obvious cause and low regression risk.

When the category is unclear, Codex should ask before implementation:

> 이건 Antigravity TDD 협업으로 갈까요, 아니면 Codex 단독으로 빠르게 처리할까요?

Default workflow for Antigravity handoff:

1. Create or confirm a GitHub issue.
2. Create a `feature/{issue-number}-{short-description}` branch from latest `main`.
3. Codex writes Agent OS spec documents and failing tests first.
4. Codex does not implement production code in the first pass.
5. Codex provides an Antigravity handoff prompt.
6. Antigravity implements production code.
7. Codex reviews, runs tests, commits, pushes, and opens/updates the PR.
