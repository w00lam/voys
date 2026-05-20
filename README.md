# voys

Project workspace for product planning, software architecture, and implementation using Agent OS.

## Agent OS

Agent OS is installed in this repository under `agent-os/`.

Useful starting points:

- `agent-os/product/architecture.md` - architecture draft and decisions
- `agent-os/product/mission.md` - product mission placeholder
- `agent-os/product/roadmap.md` - product roadmap placeholder
- `agent-os/product/tech-stack.md` - technical stack placeholder
- `.claude/commands/agent-os/` - Agent OS command definitions

## WSL Setup

This repository lives on Windows, but Agent OS scripts should be run through WSL:

```bash
cd /mnt/c/Users/woo_lam/IdeaProjects/voys
~/agent-os/scripts/project-install.sh
```

