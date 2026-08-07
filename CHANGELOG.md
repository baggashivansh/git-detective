# Changelog

All notable changes to Git Detective are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] — 2026-08-07

### Added
- Production security headers, CORS correlation/`Retry-After` exposure, and in-memory rate limiting
- Flyway `V4` production indexes for investigation and parser lookup paths
- Startup diagnostics, actuator metrics exposure
- Skip links, reduced-motion support, accessible footer
- Launch documentation suite: architecture, engines, API, demo/judging, deployment, troubleshooting
- Open-source readiness: MIT `LICENSE`, `SECURITY.md`, `CODE_OF_CONDUCT.md`, Dependabot, release workflow

### Changed
- Application version unified at **1.0.0**
- Evidence bundle engine version binds to `gitdetective.application.version`
- Final public-release documentation pass aligned to implementation

### Security
- Frame denial, nosniff, referrer policy, HSTS, permissions policy
- Rate limits on analyze / investigations / assistant POST routes

## [0.4.0] — Phase 4

Evidence-backed intelligent investigation assistant (intent → Evidence Engine → provider → validation → chat UI + SSE).

## [0.3.5] — Phase 3.5

Internal Evidence Engine and immutable Evidence Bundles.

## [0.3.0] — Phase 3

Deterministic Investigation Engine (timeline, ownership, impact, relationships, reports).

## [0.2.0] — Phase 2

Repository Intelligence (clone/scan/parse/index knowledge base).

## [0.1.0] — Phase 1

Foundation: monorepo, health API, landing + dashboard shell.

---

**Made with ❤️ by Shivansh Bagga**
