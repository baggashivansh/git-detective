# Security

Security design for Git Detective **v1.0.0**.

## v1.0 production baseline

- Input validation (Spring Validation) on request DTOs
- Centralized exception handling — no stack traces in API responses
- Spring Security filter chain with **security headers**:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - Referrer-Policy: `STRICT_ORIGIN_WHEN_CROSS_ORIGIN`
  - Permissions-Policy: `camera=(), microphone=(), geolocation=()`
  - HSTS (`max-age=31536000; includeSubDomains`) when served over HTTPS
- CORS allow-list via `CORS_ALLOWED_ORIGINS`
  - Exposed response headers: `X-Correlation-Id`, `Retry-After`
  - Allowed request headers include `Authorization`, `Content-Type`, `Accept`, `X-Correlation-Id`
- CSRF disabled for the **stateless JSON API** (no cookie session auth in v1.0)
- In-memory **rate limiting** on expensive POST routes:
  - `/repositories/analyze`
  - `/investigations*`
  - `/assistant/*`
  - Defaults: `RATE_LIMIT_MAX_REQUESTS=60`, `RATE_LIMIT_WINDOW_SECONDS=60`
  - Exceeding the limit returns `429` with `Retry-After`
- Correlation IDs on every request (`X-Correlation-Id` + MDC)
- Assistant prompt sanitization / injection filtering
- AI responses validated against Evidence Engine identifiers
- Secrets from environment variables only
- Workspace paths rooted and validated (path traversal mitigation)

## Explicitly deferred (post-1.0)

- End-user login / registration
- Authorization / roles
- Distributed (Redis) rate limiting
- Cookie-based CSRF strategy (if cookie sessions are introduced)
- Private repository OAuth / credential vaulting

## Threat mitigations

| Threat | Mitigation |
|--------|------------|
| SQL injection | JPA / parameterized queries |
| XSS | React escaping; constrained markdown rendering |
| Path traversal | Workspace path rooted + validated |
| Prompt injection | Sanitizer + filtered instructions never returned to clients |
| Secret leakage | Env vars; no credential logging |
| DoS (expensive endpoints) | Rate limiting + analysis size caps |
| Dependency vulns | Dependabot weekly updates |
| Speculative AI answers | Citation validation against Evidence Bundles |

## Analysis source constraints

- Public GitHub URLs and LOCAL git paths only
- No private repository cloning in v1.0
- Read-only investigation — the product never writes to analyzed repositories

## Reporting

See root [SECURITY.md](../SECURITY.md).

---

**Made with ❤️ by Shivansh Bagga**
