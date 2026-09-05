# Judging Guide

Five-minute orientation for hackathon judges evaluating Git Detective **v1.0.0**.

## One-liner

Git Detective turns repositories into an evidence-backed investigation platform — deterministic engineering intelligence first, AI explanation second, and never invented facts.

## Problem

Engineering teams struggle to answer: who owns this, what breaks if it changes, how did it evolve, and how do requests flow? Generic chatbots invent answers. Git hosts show history without structured investigation.

## Solution

Layered architecture:

1. **Repository Intelligence** — clone/scan/parse/index into PostgreSQL
2. **Investigation Engine** — timeline, ownership, bus factor, impact, relationships, traces
3. **Evidence Engine** — immutable, provenance-tracked bundles
4. **Assistant** — explains only from Evidence Bundles (citations validated)

```text
Repo → Intelligence → Investigation → Evidence → Assistant → UI
```

## Why it scores

| Criterion | How Git Detective demonstrates it |
|-----------|-----------------------------------|
| Technical depth | JGit + JavaParser + Flyway knowledge graph + Spring Boot 3.4 + Next.js 16 |
| Correctness | AI responses validated against evidence IDs; insufficient evidence is explicit |
| Architecture | Clean bounded contexts; AI cannot bypass Evidence Engine |
| UX | Investigation dashboards, React Flow relationships, streaming chat |
| Production readiness | Security headers, rate limits, CI, Docker, Zerops, MIT license, full docs |

## Judge walkthrough (5 minutes)

1. Open landing → **Investigate a repository**
2. Paste a public GitHub URL and a new question → Investigate
3. Watch Analyze → Collect evidence → Investigate → Validate → Report
4. Confirm claims are labeled FACT / STRONG INFERENCE / HYPOTHESIS
5. Optionally open a target-based investigation + assistant citations
6. Point to [ROTE_PLAY.md](ROTE_PLAY.md) and [DEMO_GUIDE.md](DEMO_GUIDE.md)

## Evaluation checklist

- [ ] Analysis completes without AI
- [ ] Investigation artifacts are deterministic
- [ ] Assistant cites Evidence Engine records
- [ ] Unsupported questions are rejected
- [ ] Security headers / rate limiting present
- [ ] Footer attribution present; UI usable on desktop and mobile
- [ ] Documentation complete and consistent with implementation

## Deeper reading

| Doc | Why |
|-----|-----|
| [SYSTEM_ARCHITECTURE.md](SYSTEM_ARCHITECTURE.md) | One-page system view |
| [INVESTIGATION_ENGINE.md](INVESTIGATION_ENGINE.md) | Deterministic engines |
| [EVIDENCE_ENGINE.md](EVIDENCE_ENGINE.md) | Why AI cannot invent |
| [AI_ASSISTANT.md](AI_ASSISTANT.md) | Intent → validate → stream |
| [API.md](API.md) | HTTP surface |
| [ROTE_PLAY.md](ROTE_PLAY.md) | Repo + question playbook |

---

**Made with ❤️ by Shivansh Bagga**
