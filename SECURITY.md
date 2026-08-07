# Security Policy

## Supported versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | ✅ Yes |

## Reporting a vulnerability

Please report security issues privately.

1. Open a GitHub Security Advisory for this repository, **or**
2. Email the maintainer listed in [CODEOWNERS](.github/CODEOWNERS)

Include:

- Affected component (backend / frontend / docker)
- Reproduction steps
- Impact assessment
- Whether an exploit is public

Do **not** open a public issue for vulnerabilities that could expose repositories, secrets, or AI provider keys.

## Hardening notes (v1.0)

- Stateless API; CSRF disabled by design for JSON clients
- Security headers enabled (frame deny, nosniff, referrer policy, HSTS, permissions policy)
- CORS allow-list via `CORS_ALLOWED_ORIGINS` (exposes `X-Correlation-Id`, `Retry-After`)
- In-memory rate limiting on expensive POST endpoints (`429` + `Retry-After`)
- Prompt injection filtering in the assistant
- Secrets via environment variables only — never commit `.env`
- Correlation IDs on every request (`X-Correlation-Id`)

See also [docs/SECURITY.md](docs/SECURITY.md).

---

**Made with ❤️ by Shivansh Bagga**
