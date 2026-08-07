# Development Guide

Local development for Git Detective **v1.0.0**.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+
- npm
- Docker Desktop (or compatible engine)

## First-time setup

```bash
cp .env.example .env
cp frontend/.env.example frontend/.env.local
chmod +x scripts/*.sh
./scripts/dev-up.sh
```

`dev-up.sh` starts PostgreSQL via Docker Compose and prints backend/frontend run commands.

## Running services locally

### PostgreSQL

```bash
docker compose -f docker/docker-compose.yml up -d postgres
```

### Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Stop Postgres (and other compose services) with `./scripts/dev-down.sh`.

## Useful URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Dashboard | http://localhost:3000/dashboard |
| Repositories | http://localhost:3000/repositories |
| Investigations | http://localhost:3000/investigations |
| Assistant | http://localhost:3000/assistant |
| Health | http://localhost:8080/health |
| Actuator metrics | http://localhost:8080/actuator/metrics |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

## Environment

Copy from root [`.env.example`](../.env.example). Critical locals:

| Variable | Purpose |
|----------|---------|
| `DATABASE_*` | Postgres JDBC |
| `CORS_ALLOWED_ORIGINS` | Frontend origin allow-list |
| `NEXT_PUBLIC_API_BASE_URL` | Browser → API base |
| `AI_STUB_MODE` | `true` = deterministic answers (default) |
| `AI_API_KEY` | Required only when stub mode is off |
| `WORKSPACE_ROOT` | Optional analysis workspace root |
| `RATE_LIMIT_*` | POST rate limits |

Full variable list: [README Environment variables](../README.md#environment-variables).

## Quality commands

```bash
# Backend format + style + tests
cd backend
mvn spotless:apply
mvn checkstyle:check
mvn verify

# Or from repo root
./scripts/format.sh

# Frontend
cd frontend
npm run lint
npm run build
```

## Architecture rule for contributors

```text
Repository Intelligence → Investigation → Evidence Engine → Assistant → UI
```

Assistant code must consume investigative facts **only** through `EvidenceEngine`.

See [CODING_STANDARDS.md](CODING_STANDARDS.md) and [CONTRIBUTING.md](../CONTRIBUTING.md).

## Docker full stack

```bash
docker compose -f docker/docker-compose.yml up --build
```

## Postman

Import collections as needed:

- `postman/Git-Detective-Phase1.postman_collection.json`
- `postman/Git-Detective-Phase2.postman_collection.json`
- `postman/Git-Detective-Phase3.postman_collection.json`
- `postman/Git-Detective-Phase4.postman_collection.json`

API reference: [API.md](API.md).

## Zerops

`zerops.yml` at the repository root defines backend and frontend build/run pipelines. See [DEPLOYMENT.md](DEPLOYMENT.md).

## Troubleshooting

- **Backend fails to start:** ensure PostgreSQL is healthy and credentials match `.env`
- **CORS errors:** confirm `CORS_ALLOWED_ORIGINS` includes the frontend origin
- **Frontend API URL:** set `NEXT_PUBLIC_API_BASE_URL` in `frontend/.env.local`
- More: [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

---

**Made with ❤️ by Shivansh Bagga**
