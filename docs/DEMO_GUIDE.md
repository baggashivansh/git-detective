# Demo Guide

## Goal

Show Git Detective **v1.0.0** as an evidence-backed investigation platform: index a repository, run a deterministic investigation, then ask the assistant questions grounded in Evidence Bundles.

## Prerequisites

- Java 21, Maven 3.9+, Node 22, Docker
- Fresh clone of this repository

## Setup (5 minutes)

```bash
cp .env.example .env
cp frontend/.env.example frontend/.env.local
./scripts/dev-up.sh
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
# other terminal
cd frontend && npm install && npm run dev
```

Open http://localhost:3000

Leave `AI_STUB_MODE=true` for offline demos (deterministic evidence-backed answers). For a live model, set `AI_API_KEY` and `AI_STUB_MODE=false`.

## Demo script (8–10 minutes)

1. **Landing** — Open `/`. Frame the product: investigation, not summarization.
2. **Analyze** — Go to `/repositories`. Analyze a small public GitHub Java/Spring repo (or a LOCAL path on the server).
3. **Poll status** — Open the repository dashboard; wait for `COMPLETED`.
4. **Browse knowledge** — Show tree, contributors, packages, classes, search.
5. **Investigate** — Create an investigation on a class or file. Walk Timeline, Ownership, Impact, Relationships, Hotspots, Report.
6. **Assistant** — Open the Assistant tab (or `/assistant?investigationId=`). Ask:
   - “Who owns this module?”
   - “Explain the blast radius.”
   - “What changed recently?”
7. **Evidence panel** — Expand citations; every answer is validated against Evidence Engine IDs.
8. **Negative demo** — Ask “Open a PR that fixes this” → expect `UNSUPPORTED_QUESTION` / clear rejection.
9. **Export** — Export investigation report (Markdown) and assistant conversation.

## Suggested demo repositories

- Small public Spring Boot sample (auth-flow / security package makes a strong narrative)
- This monorepo’s `backend/` as a LOCAL source (same machine as the server)

## Talking points

- Deterministic Phases 2–3; AI only in Phase 4 and only via Evidence Engine
- No repository mutation; read-only investigation
- Production polish: rate limits, security headers, correlation IDs, accessible UI
- Stub mode proves the evidence path without an external API key

## Screenshots / video

Capture and store under `docs/assets/` when available:

1. Landing hero
2. Repository analysis progress → completed
3. Investigation relationships graph
4. Assistant chat with evidence panel

Demo video placeholder: add `docs/assets/demo.mp4` or link from the README.

Live deployment placeholder: add the production URL to the README when published.

---

**Made with ❤️ by Shivansh Bagga**
