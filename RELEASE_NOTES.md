# Release Notes — v1.0.0

Git Detective 1.0.0 is the first production release.

## Highlights

- Full investigation platform: analyze → investigate → evidence → assistant
- Evidence-backed AI that cannot invent repository facts
- Production security headers, rate limiting, and observability hooks
- MIT license, complete documentation, Docker + Zerops deployment

## Upgrade notes

- Flyway migration `V4` adds production indexes (safe additive change)
- Set `APP_VERSION=1.0.0`
- Review `RATE_LIMIT_*` and `CORS_ALLOWED_ORIGINS` for your environment

## Known limitations

- No end-user authentication yet (API permit-all behind network controls)
- Rate limiter is in-memory (single instance)
- Assistant stub mode is default for local safety
- Public GitHub and LOCAL sources only (no private-repo OAuth)

## Documentation

Start with [README.md](README.md). Judges: [docs/JUDGING_GUIDE.md](docs/JUDGING_GUIDE.md). Demo: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md).

See [CHANGELOG.md](CHANGELOG.md) for the full history.

---

**Made with ❤️ by Shivansh Bagga**
