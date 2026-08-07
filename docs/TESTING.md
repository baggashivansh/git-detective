# Testing

Testing strategy for Git Detective **v1.0.0**.

## Goals

- Protect production behavior with automated tests
- Prefer fast unit tests for pure logic
- Use integration tests for HTTP + Spring context + database wiring
- Target high coverage as features land (program target: 90%+)

## Backend stack

- JUnit 5
- Mockito (via `spring-boot-starter-test`)
- Spring MockMvc
- Testcontainers (PostgreSQL)

## Repository Intelligence tests

| Test | Type | Purpose |
|------|------|---------|
| `HealthServiceTest` | Unit | Health payload metadata |
| `HealthControllerTest` | Slice (`@WebMvcTest`) | `GET /health` HTTP contract |
| `HealthControllerIntegrationTest` | Integration | Full-stack health with Testcontainers Postgres |
| `LanguageDetectorTest` | Unit | Extension → language mapping |
| `JavaSourceParserTest` | Unit | Java structural metadata extraction |
| `WorkspaceManagerTest` | Unit (Mockito) | Workspace create/cleanup + duplicate prevention |
| `GitEngineTest` | Unit/integration-lite | Local git metadata + URL validation |
| `RepositoryControllerTest` | Slice | Analyze + search HTTP contracts |
| `RepositoryAnalysisIntegrationTest` | Integration | End-to-end local analysis with Testcontainers |

## Investigation tests

| Test | Type | Purpose |
|------|------|---------|
| `OwnershipEngineTest` | Unit | Ownership metrics + bus factor levels |
| `ImpactEngineTest` | Unit | Blast radius / depth / score from edges |
| `TimelineEngineTest` | Unit | Chronological event construction |
| `InvestigationReportExporterTest` | Unit | JSON / Markdown / HTML export |
| `InvestigationControllerTest` | Slice | Investigation HTTP contracts |

## Evidence Engine tests

| Test | Type | Purpose |
|------|------|---------|
| `EvidenceBundleBuilderTest` | Unit | Bundle assembly, mismatch, cache flag |
| `EvidenceValidatorTest` | Unit | Duplicates, confidence, repo mismatch |
| `InvestigationEvidenceMapperTest` | Unit | Type / ref mapping |
| `CollectorsTest` | Unit | Timeline / ownership / relationship collectors |
| `InMemoryEvidenceBundleCacheTest` | Unit | Cache put / get / invalidate |
| `EvidenceBundleServiceTest` | Unit (Mockito) | Cache, incomplete investigation, bypass |
| `EvidenceEngineTest` | Unit (Mockito) | Facade delegation |

## Assistant tests

| Test | Type | Purpose |
|------|------|---------|
| `IntentDetectorTest` | Unit | Deterministic intent classification |
| `PromptBuilderTest` | Unit | Sanitization + prompt sections |
| `EvidenceContextBuilderTest` | Unit | Compact context from EvidenceBundle |
| `AssistantEvidenceValidatorTest` | Unit | Cite-known / reject-unknown evidence |
| `OpenAiCompatibleAiProviderTest` | Unit | Stub completion + local streaming |
| `AssistantResponseFormatterTest` | Unit | Citations + follow-ups |
| `AssistantControllerTest` | Slice | Conversation + ask HTTP contracts |

## Production hardening tests

| Test | Type | Purpose |
|------|------|---------|
| `RateLimitFilterTest` | Unit | Rate limit allow / block / GET skip |

Integration tests use `@Testcontainers(disabledWithoutDocker = true)` so local runs without Docker skip them rather than fail. CI runners with Docker execute them.

## Running tests

```bash
cd backend
mvn test
# full verify including packaging
mvn verify
```

Integration tests require Docker because Testcontainers starts PostgreSQL.

## Frontend

There is no frontend unit-test runner in v1.0. Quality gates:

```bash
cd frontend
npm run lint
npm run build
```

Accessibility is verified via skip links, ARIA on interactive controls, and `prefers-reduced-motion` support.

## Conventions

- Name tests by behavior, not by implementation detail
- Cover happy path, invalid input, and failure modes for each production feature
- Do not hit external production systems from tests
- Keep test data local and deterministic
- Investigation engines must remain AI-free; assistant tests assert Evidence Engine citations
- Prefer stub AI mode (`AI_STUB_MODE=true`) in automated environments

---

**Made with ❤️ by Shivansh Bagga**
