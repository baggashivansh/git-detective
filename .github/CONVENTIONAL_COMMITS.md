# Conventional Commit Guide

Git Detective uses [Conventional Commits](https://www.conventionalcommits.org/).

## Format

```text
<type>(optional-scope): <description>

[optional body]

[optional footer]
```

## Allowed types

| Type | When to use |
|------|-------------|
| `feat` | A new user-facing or API capability |
| `fix` | A bug fix |
| `docs` | Documentation only |
| `style` | Formatting that does not change meaning |
| `refactor` | Code change that is not a fix or feature |
| `test` | Adding or correcting tests |
| `chore` | Maintenance, tooling, dependency bumps |
| `ci` | CI/CD configuration |
| `build` | Build system or packaging changes |
| `perf` | Performance improvement |

## Scopes (suggested)

- `backend`
- `frontend`
- `docker`
- `docs`
- `security`
- `ci`

## Examples

```text
feat(backend): add health endpoint
docs: document Phase 1 architecture
chore(frontend): upgrade Next.js
ci: add backend and frontend verify workflow
```

## Rules

1. Use the imperative mood (`add`, not `added`).
2. Keep the subject under 72 characters when practical.
3. Do not mention Phase 2+ work in Phase 1 commits unless explicitly scoped.
4. Breaking changes must include `BREAKING CHANGE:` in the footer.
