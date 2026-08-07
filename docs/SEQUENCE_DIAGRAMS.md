# Sequence Diagrams

Request lifecycles for Git Detective **v1.0.0**.

## Repository analysis

```mermaid
sequenceDiagram
    participant UI as Frontend
    participant API as RepositoryController
    participant AS as AnalysisService
    participant WS as WorkspaceManager
    participant GE as GitEngine
    participant IX as Indexer/Parser/Graph
    participant DB as PostgreSQL

    UI->>API: POST /repositories/analyze
    API->>AS: queue analysis
    AS-->>UI: 202 + repository id
    AS->>WS: create workspace
    AS->>GE: clone or copy + collect metadata
    AS->>IX: scan, parse, persist graph
    IX->>DB: write knowledge base
    AS->>WS: cleanup workspace
    UI->>API: GET /repositories/{id} (poll)
    API-->>UI: status COMPLETED
```

Progress states: `QUEUED → CLONING → SCANNING → PARSING → INDEXING → COMPLETED | FAILED`

## Investigation

```mermaid
sequenceDiagram
    participant UI as Frontend
    participant API as InvestigationController
    participant IS as InvestigationService
    participant Engines as Timeline/Ownership/Impact/...
    participant DB as PostgreSQL

    UI->>API: POST /investigations
    API->>IS: create(target)
    IS->>DB: load COMPLETED repository index
    IS->>Engines: compute deterministic slices
    Engines->>DB: persist investigation_* artifacts
    IS-->>UI: summary COMPLETED
```

## Evidence gather (internal)

```mermaid
sequenceDiagram
    participant AS as AssistantService
    participant EE as EvidenceEngine
    participant Cache as EvidenceBundleCache
    participant Bld as EvidenceBundleBuilder

    AS->>EE: gather(investigationId)
    alt cache hit
        EE->>Cache: get
        Cache-->>EE: EvidenceBundle
    else miss
        EE->>Bld: build from investigation detail
        Bld-->>EE: EvidenceBundle
        EE->>Cache: put
    end
    EE-->>AS: EvidenceBundle
```

## Evidence-backed assistant (blocking)

```mermaid
sequenceDiagram
    participant UI as Frontend
    participant API as AssistantController
    participant AS as AssistantService
    participant ID as IntentDetector
    participant EE as EvidenceEngine
    participant AI as AiProvider
    participant VAL as AssistantEvidenceValidator

    UI->>API: POST /assistant/conversations/{id}/messages
    API->>AS: ask(question)
    AS->>ID: detect intent
    AS->>EE: gather(investigationId)
    EE-->>AS: EvidenceBundle
    AS->>AI: complete(prompt)
    AI-->>AS: JSON answer
    AS->>VAL: validate citations
    VAL-->>AS: verified
    AS-->>UI: AssistantAnswer + evidence
```

## Streaming ask (SSE)

```mermaid
sequenceDiagram
    participant UI as Frontend
    participant API as AssistantController
    participant AS as AssistantService

    UI->>API: POST .../messages/stream
    API-->>UI: event:intent
    API-->>UI: event:token*
    API-->>UI: event:answer
    API-->>UI: event:done
    Note over UI,API: POST .../cancel may emit cancelled
```

## Correlation & rate limit (cross-cutting)

Every request receives `X-Correlation-Id`. Expensive POSTs (`/repositories/analyze`, `/investigations*`, `/assistant/*`) may return `429` with `Retry-After`.

---

**Made with ❤️ by Shivansh Bagga**
