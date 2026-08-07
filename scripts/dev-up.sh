#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Starting PostgreSQL via Docker Compose..."
docker compose -f "${ROOT_DIR}/docker/docker-compose.yml" up -d postgres

echo "Waiting for PostgreSQL health..."
until docker compose -f "${ROOT_DIR}/docker/docker-compose.yml" exec -T postgres pg_isready -U gitdetective -d gitdetective >/dev/null 2>&1; do
  sleep 1
done

echo "PostgreSQL is ready."
echo "Start backend:  cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
echo "Start frontend: cd frontend && npm run dev"
