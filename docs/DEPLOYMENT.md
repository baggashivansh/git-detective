# Deployment Guide

Deploying Git Detective **v1.0.0**.

## Environments

| Profile | Purpose |
|---------|---------|
| `dev` | Local development |
| `prod` | Container / Zerops production |
| `test` | Automated tests |

## Docker Compose

```bash
cp .env.example .env
docker compose -f docker/docker-compose.yml up --build
```

Services (`docker/docker-compose.yml`):

| Service | Default port | Notes |
|---------|--------------|-------|
| `postgres` | `5432` | PostgreSQL 16 |
| `backend` | `8080` | Health `GET /health` |
| `frontend` | `3000` | Next.js |

Set at minimum: `APP_VERSION=1.0.0`, `CORS_ALLOWED_ORIGINS`, database credentials. For live AI: `AI_STUB_MODE=false` and `AI_API_KEY`.

Compose currently wires database, CORS, and `APP_VERSION`. Pass additional AI / rate-limit variables via environment or an env file when needed.

## Zerops

Root `zerops.yml` defines two setups:

### Backend

- Base: `java@21`
- Build: `cd backend && mvn -q -DskipTests package`
- Artifact: `backend/target/git-detective-backend.jar`
- Start: `java -jar backend/target/git-detective-backend.jar`
- Readiness: `GET` port `8080` path `/health`
- Env: `SERVER_PORT=8080`, `APP_VERSION=1.0.0`, `SPRING_PROFILES_ACTIVE=prod`

### Frontend

- Base: `nodejs@22`
- Build: `cd frontend && npm ci && npm run build`
- Deploy: `.next/standalone`, `.next/static`, `public`
- Start: `node frontend/.next/standalone/server.js`
- Env: `NODE_ENV=production`, `PORT=3000`, `HOSTNAME=0.0.0.0`

PostgreSQL is provisioned separately in the Zerops project. Configure secrets there — never commit them:

- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- `CORS_ALLOWED_ORIGINS` (production frontend origin)
- `AI_API_KEY`, `AI_STUB_MODE` (typically `false` in prod when a key is present)
- `NEXT_PUBLIC_API_BASE_URL` at frontend build time (public API URL)

## Production checklist

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] Strong Postgres credentials
- [ ] CORS allow-list matches the frontend origin
- [ ] `AI_STUB_MODE=false` only when `AI_API_KEY` is set
- [ ] Rate limits tuned (`RATE_LIMIT_MAX_REQUESTS`, `RATE_LIMIT_WINDOW_SECONDS`)
- [ ] HTTPS terminator / reverse proxy in front of services
- [ ] Actuator limited to `health,info,metrics` (no detail exposure)
- [ ] Workspace disk sized for clone limits (`MAX_REPOSITORY_SIZE_BYTES`, etc.)

## Health & observability

| Endpoint | Purpose |
|----------|---------|
| `GET /health` | Application health (`status`, `name`, `version`) |
| `GET /actuator/health` | Spring Actuator health |
| `GET /actuator/info` | Actuator info |
| `GET /actuator/metrics` | Metrics catalog |

Every response includes `X-Correlation-Id` for log correlation.

## CI / CD

- GitHub Actions: `.github/workflows/ci.yml` (quality gates)
- Release workflow: `.github/workflows/release.yml`
- Dependabot: `.github/dependabot.yml`

---

**Made with ❤️ by Shivansh Bagga**
