#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Formatting backend with Spotless..."
(cd "${ROOT_DIR}/backend" && mvn -q spotless:apply)

echo "Linting frontend with ESLint..."
(cd "${ROOT_DIR}/frontend" && npm run lint -- --max-warnings=0)

echo "Format pass complete."
