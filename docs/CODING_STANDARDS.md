# Coding Standards

Standards for Git Detective **v1.0.0** contributions.

## General

- Prefer readability over cleverness
- Keep methods short and single-purpose
- Avoid magic strings/numbers; use named constants or configuration
- No dead code, commented-out blocks, or placeholder business logic
- Comments explain **why**, not what
- Documentation must match implementation — never invent features

## Architecture boundary

```text
Repository Intelligence → Investigation → Evidence Engine → Assistant → UI
```

Assistant code must not call Git, parsers, investigation engines, or JPA entities for investigative facts.

## Backend (Java)

- Java 21 language level
- Constructor injection over field injection
- Controllers stay thin; business orchestration lives in services
- DTOs for API boundaries; entities remain persistence concerns
- MapStruct for mapping when entity/DTO pairs exist
- Lombok allowed for boilerplate reduction; do not hide important behavior
- Spotless (Google Java Format AOSP) is mandatory
- Checkstyle must pass in CI

## Frontend (TypeScript / React)

- Prefer server components by default; mark client components explicitly
- Colocate UI by feature (`landing`, `layout`, `providers`, domain folders)
- Use Zod for runtime validation of external/config values where applicable
- React Query for server-state
- Avoid inventing fake domain data

## Naming

Prefer:

- `HealthService`
- `GlobalExceptionHandler`
- `EvidenceEngine`
- `SiteFooter`

Avoid:

- Catch-all `Utils` / `CommonHelper` / `TempService`
- Abbreviations that are not industry-standard

`com.gitdetective.util` and `com.gitdetective.validation` are reserved empty packages — prefer named domain packages.

## Error handling

- Never swallow exceptions silently
- Use structured API error envelopes
- Log failures with correlation context (`X-Correlation-Id`)
- Never log secrets, tokens, or credentials
- Never return stack traces to clients

## Formatting

- EditorConfig defines baseline whitespace rules
- Backend formatting via Spotless
- Frontend via ESLint (`npm run lint`)

## Local gates

```bash
cd backend && mvn spotless:apply && mvn checkstyle:check && mvn verify
cd frontend && npm run lint && npm run build
```

---

**Made with ❤️ by Shivansh Bagga**
