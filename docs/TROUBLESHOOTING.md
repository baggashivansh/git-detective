# Troubleshooting Guide

Common failures for Git Detective **v1.0.0**.

## Backend will not start

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Flyway checksum / migration error | DB from older schema | Reset local DB volume or repair migrations carefully |
| Connection refused to Postgres | DB not running | `./scripts/dev-up.sh` |
| Port 8080 in use | Another process | Change `SERVER_PORT` / `BACKEND_PORT` |
| Profile mismatch | Wrong Spring profile | Use `dev` locally, `prod` in containers |

## Analysis stuck / FAILED

- Confirm source is a **public** GitHub URL or a valid LOCAL git path on the **server** filesystem
- Private repositories, GitLab, Bitbucket, and ZIP uploads are unsupported
- Check size limits: `CLONE_TIMEOUT_SECONDS`, `MAX_REPOSITORY_SIZE_BYTES`, `MAX_FILES`, `MAX_COMMITS`
- Inspect `GET /repositories/{id}` for `errorMessage` / `statusMessage`
- Ensure disk space for the workspace root (`WORKSPACE_ROOT`)

## Investigation `409 REPOSITORY_NOT_READY`

Repository analysis must reach `COMPLETED` before creating investigations.

## Assistant errors

| Error | Meaning |
|-------|---------|
| `UNSUPPORTED_QUESTION` | Outside investigation assistance scope (e.g. “generate a PR”) |
| `AI_RESPONSE_INVALID` | Model output failed evidence validation — retry |
| `AI_PROVIDER_ERROR` | Upstream provider failure — check key/URL or enable stub mode |
| `RATE_LIMITED` | Too many POST requests — wait for `Retry-After` / tune `RATE_LIMIT_*` |

Leave `AI_STUB_MODE=true` for offline / CI demos. For live models set `AI_API_KEY` and `AI_STUB_MODE=false`.

## Frontend cannot reach API

- `NEXT_PUBLIC_API_BASE_URL` must match the backend origin
- CORS must include the frontend origin (`CORS_ALLOWED_ORIGINS`)
- Check the browser network tab for `X-Correlation-Id` on responses
- Rebuild the frontend after changing `NEXT_PUBLIC_*` (build-time vars)

## CORS / rate limit headers missing in browser

CORS must expose `X-Correlation-Id` and `Retry-After`. Confirm origins match exactly (scheme + host + port).

## Testcontainers skipped

Integration tests skip when Docker is unavailable (`disabledWithoutDocker = true`). Start Docker Desktop for full integration coverage.

## Streaming chat hangs

- Cancel via Stop (`POST /assistant/conversations/{id}/cancel`)
- Prefer the blocking ask mode if a proxy buffers SSE
- Confirm intermediaries do not strip `text/event-stream`

## Docker Compose issues

```bash
docker compose -f docker/docker-compose.yml logs backend
docker compose -f docker/docker-compose.yml ps
```

Ensure `.env` exists (copy from `.env.example`) and ports `5432`, `8080`, `3000` are free.

## Zerops

- Backend readiness must succeed on `/health`
- Frontend `NEXT_PUBLIC_API_BASE_URL` must point at the public backend URL at **build** time
- Database credentials must be set as Zerops secrets

More setup detail: [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md), [DEPLOYMENT.md](DEPLOYMENT.md).

---

**Made with ❤️ by Shivansh Bagga**
