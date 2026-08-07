# Project Vision

## What Git Detective is

Git Detective is a professional software investigation platform. It helps engineering teams answer high-value questions about their codebase:

- Why does this code exist?
- Who changed it, and when?
- Which files and services are affected?
- What may break if this changes?
- How did the architecture evolve?
- How do requests flow through the system?

## What Git Detective is not

- Not a GitHub clone
- Not a chatbot interface
- Not a generic repository summarizer

## Product principles

1. Evidence over assumptions
2. Investigation over summarization
3. Production maintainability over prototype velocity
4. Clear architecture boundaries
5. Security and observability from day one

## Architecture principle

```text
Repository Intelligence → Investigation → Evidence Engine → Assistant → UI
```

Facts are computed first. AI explains only after evidence is packaged and validated. If evidence is insufficient, the product says so.

## Phase strategy

Development proceeded in sequential phases. Each phase was completed and verified before the next began.

| Phase | Scope | Status |
|-------|-------|--------|
| 1 | Foundation & production architecture | Complete |
| 2 | Repository Intelligence Engine | Complete |
| 3 | Investigation Engine (deterministic, no AI) | Complete |
| 3.5 | Evidence Engine (immutable Evidence Bundles) | Complete |
| 4 | Intelligent Investigation Assistant | Complete |
| 5 | Production polish, docs, launch readiness | Complete — **v1.0.0** |

## Version

The product is **Version 1.0.0**.

## Post-1.0 direction

Candidates (not implemented): authentication, Redis-backed caches/rate limits, private repositories, additional language parsers, PDF binary export.

---

**Made with ❤️ by Shivansh Bagga**
