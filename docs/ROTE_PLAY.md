# Rote Play — Incident Investigator

Reusable play for someone who has never seen this repository.

## Input

```text
repository              public GitHub URL, or an already-analyzed repositoryId
investigation_question  what you need to understand from repository evidence
```

Example:

```text
repository: https://github.com/baggashivansh/DSA-Java-Shiv
investigation_question: Why did authentication become risky after recent changes?
```

## Process

```text
analyze            clone/index the repository, or reuse a completed analysis
collect evidence   run timeline, ownership, impact, traces, hotspots
investigate        bind the question to an indexed target
validate           AI may only cite Evidence Bundle records
report             structured findings with FACT / STRONG INFERENCE / HYPOTHESIS
```

The play never invents commits, files, contributors, timestamps, ownership, or causality. Ownership is code ownership, not personal blame. Repository timestamps are not deployment or incident clocks.

## Output

An evidence-backed investigation report:

- Finding
- Confidence
- Why
- Timeline
- Key evidence
- Affected files/components
- Code ownership
- Blast radius
- Risk factors
- Recommended next investigation/actions
- Limitations

If evidence is insufficient, the report says so.

## Run it

### UI

1. Open `/investigate`
2. Paste a public GitHub URL
3. Enter a new investigation question
4. Click **Investigate**
5. Wait for Analyze → Collect evidence → Investigate → Validate → Report

### API

```bash
API=http://localhost:8080

curl -sS -X POST "$API/incident-investigations" \
  -H 'Content-Type: application/json' \
  -d '{
    "repository": "https://github.com/baggashivansh/DSA-Java-Shiv",
    "investigationQuestion": "Why did authentication become risky after recent changes?"
  }'
```

If `phase` is `ANALYZING`, poll repository progress then continue:

```bash
curl -sS -X POST "$API/incident-investigations/<id>/continue"
curl -sS "$API/incident-investigations/<id>"
```

`COMPLETED` includes `report`. The same repository + question returns the stored report (no rescan).

### Local stack

```bash
cp .env.example .env
./scripts/dev-up.sh
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd frontend && npm install && npm run dev
```

Open http://localhost:3000/investigate

Leave `AI_STUB_MODE=true` for offline/CI. The stub still has to cite evidence IDs; invented citations are discarded.

## What is reused

- Repository analysis (`POST /repositories/analyze`)
- Investigation engines (timeline, ownership, impact, traces, hotspots)
- Evidence Engine (`EvidenceEngine.gather`)
- Assistant citation validator and AI provider
- Existing security: GitHub URL allow-list, JGit-only clone, prompt sanitization, rate limits
