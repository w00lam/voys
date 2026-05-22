# voys

Project workspace for product planning, software architecture, and implementation using Agent OS.

## Agent OS

Agent OS is installed in this repository under `agent-os/`.

Useful starting points:

- `agent-os/product/architecture.md` - architecture draft and decisions
- `agent-os/product/mission.md` - product mission placeholder
- `agent-os/product/roadmap.md` - product roadmap placeholder
- `agent-os/product/tech-stack.md` - technical stack placeholder
- `agent-os/product/mvp-verification.md` - MVP manual verification checklist

## WSL Setup

This repository lives on Windows, but Agent OS scripts should be run through WSL:

```bash
cd /mnt/c/Users/woo_lam/IdeaProjects/voys
~/agent-os/scripts/project-install.sh
```

## Local Development

Backend:

```bash
docker compose up -d postgres
cd backend
./gradlew bootRun
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to the Spring Boot backend at `http://localhost:8080`.
