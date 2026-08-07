# HTTP API Reference

Git Detective **v1.0.0** exposes a JSON API (plus SSE for assistant streaming). Successful responses use the `ApiResponse` envelope:

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "timestamp": "2026-08-07T00:00:00Z"
}
```

Errors return a structured error body (`success: false`, `errorCode`, `message`). Stack traces are never included.

Interactive docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Postman collections: `postman/Git-Detective-Phase{1,2,3,4}.postman_collection.json`

---

## Health

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/health` | Application health (`status`, `name`, `version`, `timestamp`) |

---

## Repositories

Base path: `/repositories`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/repositories/analyze` | Queue async analysis (`202`) |
| `GET` | `/repositories` | List repositories |
| `GET` | `/repositories/{id}` | Summary + progress |
| `GET` | `/repositories/{id}/tree` | File/folder tree |
| `GET` | `/repositories/{id}/contributors` | Contributors |
| `GET` | `/repositories/{id}/languages` | Language stats |
| `GET` | `/repositories/{id}/commits` | Commits |
| `GET` | `/repositories/{id}/statistics` | Aggregate statistics |
| `GET` | `/repositories/{id}/packages` | Packages |
| `GET` | `/repositories/{id}/classes` | Types (classes/interfaces/enums) |
| `GET` | `/repositories/{id}/search?q=` | Search indexed metadata |

### Analyze body

```json
{
  "sourceType": "GITHUB",
  "source": "https://github.com/owner/repo"
}
```

| `sourceType` | `source` |
|--------------|----------|
| `GITHUB` | Public `github.com` repository URL |
| `LOCAL` | Absolute path to a git repository on the **server** filesystem |

Unsupported: private repositories, GitLab, Bitbucket, ZIP uploads.

### Analysis status

`QUEUED` → `CLONING` → `SCANNING` → `PARSING` → `INDEXING` → `COMPLETED` | `FAILED`

---

## Investigations

Base path: `/investigations`  
Requires repository `AnalysisStatus.COMPLETED`.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/investigations` | Create investigation |
| `GET` | `/investigations` | List investigations |
| `GET` | `/investigations/{id}` | Full detail |
| `GET` | `/investigations/{id}/timeline` | Timeline slice |
| `GET` | `/investigations/{id}/ownership` | Ownership slice |
| `GET` | `/investigations/{id}/impact` | Impact slice |
| `GET` | `/investigations/{id}/relationships` | Relationships slice |
| `GET` | `/investigations/{id}/report?format=` | Report export |

### Create body

```json
{
  "repositoryId": "<uuid>",
  "targetType": "CLASS",
  "targetRef": "com.example.Demo"
}
```

`targetType`: `CLASS` | `METHOD` | `PACKAGE` | `COMMIT` | `FILE` | `CONTRIBUTOR` | `BRANCH` | `TAG`

`format`: `json` | `markdown` | `html`

---

## Assistant

Base path: `/assistant`  
Conversations are scoped to one investigation. Investigative data comes from `EvidenceEngine` only.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/assistant/conversations` | Create conversation `{ "investigationId" }` |
| `GET` | `/assistant/conversations?investigationId=` | List |
| `GET` | `/assistant/conversations/{id}` | Detail + messages |
| `POST` | `/assistant/conversations/{id}/messages` | Ask (blocking) |
| `POST` | `/assistant/conversations/{id}/messages/stream` | Ask (SSE) |
| `POST` | `/assistant/conversations/{id}/cancel` | Cancel stream |
| `GET` | `/assistant/conversations/{id}/suggestions` | Deterministic suggestions |
| `GET` | `/assistant/conversations/{id}/export?format=` | `markdown` \| `json` \| `html` |

### Ask body

```json
{ "question": "Who owns this module?" }
```

### Assistant answer (data)

Includes: `answer`, `evidenceUsed[]`, `confidence`, supporting artifacts, referenced files/commits/contributors/packages, `suggestedFollowUpQuestions`, `intent`, `insufficientEvidence`.

### SSE events

`intent` → `token*` → `answer` → `done` (or `error` / `cancelled`)

---

## Operations

| Path | Description |
|------|-------------|
| `GET /actuator/health` | Actuator health |
| `GET /actuator/info` | Actuator info |
| `GET /actuator/metrics` | Metrics catalog |

---

## Rate limiting

POST requests to `/repositories/analyze`, `/investigations*`, and `/assistant/*` are rate-limited (default 60 / 60s). Exceeding the limit returns `429` with `Retry-After`.

---

## Correlation

Every response includes `X-Correlation-Id` (accepted on request if provided).

---

**Made with ❤️ by Shivansh Bagga**
