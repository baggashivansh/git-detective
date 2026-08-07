# Contributing

Thank you for contributing to Git Detective.

## Principles

1. Production quality over speed.
2. Prefer clarity over cleverness.
3. Ask for clarification instead of assuming requirements.
4. Keep docs synchronized with code.
5. Follow the [Code of Conduct](CODE_OF_CONDUCT.md).
6. Respect the architecture boundary:

```text
Repository Intelligence → Investigation → Evidence Engine → Assistant → UI
```

The Assistant must consume investigative facts **only** via `EvidenceEngine`.

## Good first contributions

- Documentation clarifications
- Accessibility improvements
- Additional unit tests for engines / assistant validators
- Example repositories for the demo guide
- Screenshot assets under `docs/assets/`

See [.github/LABELS.md](.github/LABELS.md) for triage labels.

## Workflow

1. Create a focused branch from `main`.
2. Implement only the approved scope.
3. Add or update tests.
4. Update documentation when behavior or structure changes.
5. Open a pull request using the PR template.
6. Ensure CI is green.

## Commit messages

Follow [.github/CONVENTIONAL_COMMITS.md](.github/CONVENTIONAL_COMMITS.md).

## Local checks

### Backend

```bash
cd backend
mvn spotless:apply
mvn checkstyle:check
mvn verify
```

### Frontend

```bash
cd frontend
npm run lint
npm run build
```

Or from the repository root: `./scripts/format.sh`

## Pull requests

- Describe why the change exists.
- List verification steps.
- Call out any intentionally deferred work.
- Do not invent features in documentation — implementation is the source of truth.
- Do not introduce AI paths that bypass the Evidence Engine.

## Documentation

When changing APIs, engines, env vars, or deployment, update the relevant docs under `docs/` and the README documentation map if needed.

Developer onboarding: [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md)  
Coding standards: [docs/CODING_STANDARDS.md](docs/CODING_STANDARDS.md)

## Code ownership

See [.github/CODEOWNERS](.github/CODEOWNERS).

---

**Made with ❤️ by Shivansh Bagga**
