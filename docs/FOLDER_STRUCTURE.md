# Folder Structure

Git Detective **v1.0.0** monorepo layout (source of truth: the repository tree).

```text
git-detective/
├── backend/
│   ├── checkstyle.xml
│   ├── pom.xml
│   ├── sonar-project.properties
│   └── src/
│       ├── main/
│       │   ├── java/com/gitdetective/
│       │   │   ├── GitDetectiveApplication.java
│       │   │   ├── analyzer/
│       │   │   ├── assistant/
│       │   │   ├── common/
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── evidence/
│       │   │   ├── exception/
│       │   │   ├── git/
│       │   │   ├── graph/
│       │   │   ├── history/
│       │   │   ├── impact/
│       │   │   ├── indexer/
│       │   │   ├── investigation/
│       │   │   ├── logging/
│       │   │   ├── mapper/
│       │   │   ├── ownership/
│       │   │   ├── parser/
│       │   │   ├── relationship/
│       │   │   ├── repository/
│       │   │   ├── security/
│       │   │   ├── service/
│       │   │   ├── timeline/
│       │   │   ├── trace/
│       │   │   ├── util/          # reserved (empty)
│       │   │   ├── validation/    # reserved (empty)
│       │   │   └── workspace/
│       │   └── resources/
│       │       └── db/migration/   # Flyway V1–V4
│       └── test/
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── assistant/
│   │   │   ├── dashboard/
│   │   │   ├── investigations/
│   │   │   └── repositories/
│   │   ├── components/
│   │   │   ├── chat/
│   │   │   ├── evidence/
│   │   │   ├── investigation/
│   │   │   ├── landing/
│   │   │   ├── layout/
│   │   │   ├── messages/
│   │   │   ├── providers/
│   │   │   ├── repository/
│   │   │   └── ui/
│   │   ├── features/
│   │   │   ├── assistant/
│   │   │   ├── investigation/
│   │   │   └── repository/
│   │   ├── services/
│   │   ├── types/
│   │   └── lib/
│   ├── components.json
│   └── package.json
├── docs/
│   ├── AI_ASSISTANT.md
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── CODING_STANDARDS.md
│   ├── COMPONENT_DIAGRAM.md
│   ├── DEMO_GUIDE.md
│   ├── DEPLOYMENT.md
│   ├── DEVELOPMENT_GUIDE.md
│   ├── EVIDENCE_ENGINE.md
│   ├── FOLDER_STRUCTURE.md
│   ├── INVESTIGATION_ENGINE.md
│   ├── JUDGING_GUIDE.md
│   ├── PROJECT_VISION.md
│   ├── SECURITY.md
│   ├── SEQUENCE_DIAGRAMS.md
│   ├── SYSTEM_ARCHITECTURE.md
│   ├── TESTING.md
│   ├── TROUBLESHOOTING.md
│   └── assets/                    # screenshots / logo placeholders
├── docker/
│   ├── backend.Dockerfile
│   ├── frontend.Dockerfile
│   ├── docker-compose.yml
│   └── .env.example
├── scripts/
│   ├── dev-up.sh
│   ├── dev-down.sh
│   └── format.sh
├── postman/
├── .github/
│   ├── workflows/
│   ├── ISSUE_TEMPLATE/
│   ├── dependabot.yml
│   └── LABELS.md
├── CHANGELOG.md
├── RELEASE_NOTES.md
├── LICENSE
├── SECURITY.md
├── CODE_OF_CONDUCT.md
├── CONTRIBUTING.md
├── zerops.yml
├── .env.example
└── README.md
```

## Naming rules

- One responsibility per package/directory
- No catch-all folders (`temp`, `misc`, `utils2`)
- Domain engines live in named packages (`timeline`, `ownership`, `impact`, …)
- `com.gitdetective.repository` is reserved for Spring Data JPA repositories
- `util` and `validation` are reserved stubs — prefer named domain packages for new code

---

**Made with ❤️ by Shivansh Bagga**
