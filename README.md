# AI Code Review Assistant

Full-stack Java code review tool: upload a `.java` file or a zipped project,
get it run through **Checkstyle + PMD + SpotBugs**, and receive an **AI-generated
summary** plus a deterministic 0–100 quality score, viewable in the app and
exportable as a PDF report.

Built exactly to the tech stack and structure you specified:

| Layer | Technology |
|---|---|
| Frontend | React.js + Tailwind CSS |
| Backend | Spring Boot (Java 17) |
| Build Tool | Maven |
| Database | PostgreSQL (Supabase-compatible) |
| ORM | Spring Data JPA (Hibernate) |
| Auth | Spring Security + JWT |
| AI Integration | OpenAI-compatible chat completions API |
| Static Analysis | Checkstyle, PMD, SpotBugs (run in-process, not shelled out) |
| File Upload | Spring `MultipartFile` |
| PDF Reports | OpenPDF |
| Deployment | Railway / Render (backend), Vercel (frontend) |

---

## 1. Project structure

```
ai-code-review-assistant/
├── backend/
│   ├── src/main/java/com/astra/codereview/
│   │   ├── controller/     AuthController, ProjectController, ReviewController
│   │   ├── service/        AuthService, ProjectService, ReviewService,
│   │   │                   ScoringService, AiReviewService, PdfReportService
│   │   ├── service/analysis/  CheckstyleAnalysisService, PmdAnalysisService,
│   │   │                      SpotBugsAnalysisService, JavaCompilerService,
│   │   │                      StaticAnalysisAggregatorService
│   │   ├── repository/     UserRepository, ProjectRepository, ReviewRepository,
│   │   │                   ReviewFindingRepository
│   │   ├── entity/         User, Project, Review, ReviewFinding, Severity, UploadType
│   │   ├── dto/            AuthDtos, ProjectDto, ReviewDto, FindingDto, RawFinding
│   │   ├── security/       JwtUtil, JwtAuthFilter, AppUserDetailsService, AppUserPrincipal
│   │   ├── util/           FileStorageUtil
│   │   ├── config/         SecurityConfig, OpenAiConfig
│   │   └── exception/      ApiExceptions, GlobalExceptionHandler
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── checkstyle/astra_checks.xml
│   ├── uploads/   (uploaded source lands here at runtime)
│   ├── reports/   (generated PDFs land here at runtime)
│   ├── pom.xml
│   └── Procfile   (Railway)
├── frontend/
│   ├── src/
│   │   ├── pages/       LoginPage, RegisterPage, DashboardPage, UploadPage, ReviewResultPage
│   │   ├── components/  Navbar, FileDropzone, ScoreGauge, SeverityBadge, FindingCard,
│   │   │                Loader, ProtectedRoute
│   │   ├── services/    api.js, authService.js, projectService.js, reviewService.js
│   │   └── context/     AuthContext.jsx
│   ├── package.json, tailwind.config.js, postcss.config.js
│   └── vercel.json
└── render.yaml   (Render, backend)
```

## 2. Database design (as specified)

```
users            (id, name, email, password, created_at)
projects         (id, user_id, project_name, upload_type, created_at)   [+ storage_path]
reviews          (id, project_id, review_score, summary, created_at)    [+ status]
review_findings  (id, review_id, severity, issue, explanation,
                   suggestion, file_name, line_number)                  [+ source_tool]
```
`storage_path`, `status`, and `source_tool` were added because the analysis
engines and PDF export need them; everything you listed is present.
Hibernate creates/updates these tables automatically (`ddl-auto=update`).

## 3. How the review pipeline actually works

1. **Upload** (`POST /api/projects/upload`) — accepts a `.java` file or a
   `.zip`. Zips are extracted with zip-slip protection into
   `uploads/{userId}/{uuid}/`.
2. **Run review** (`POST /api/reviews/project/{projectId}`):
   - `StaticAnalysisAggregatorService` walks the source tree and runs:
     - **Checkstyle** — in-process, against the bundled `astra_checks.xml`
       ruleset (naming, complexity, imports, best practices).
     - **PMD** — in-process via the PMD 7 `PmdAnalysis` API, using the
       `bestpractices`, `errorprone`, `design`, and `performance` rule
       categories.
     - **SpotBugs** — requires compiled bytecode, so `JavaCompilerService`
       first compiles the uploaded `.java` files with the JDK's in-process
       compiler. **If the upload doesn't compile in isolation** (e.g. it
       depends on third-party libraries that weren't uploaded), SpotBugs is
       honestly skipped with an `INFO` finding explaining why — it does not
       pretend to have analyzed bytecode it never produced. Checkstyle and
       PMD still run and deliver full value on source alone.
   - `ScoringService` computes the 0–100 score **deterministically** from
     severities (Critical −15, High −8, Medium −3, Low −1, Info −0), so
     the score never depends on the LLM and re-running on unchanged code
     is stable.
   - `AiReviewService` sends the aggregated findings (already scored) to an
     OpenAI-compatible `/chat/completions` endpoint to generate a short,
     prioritized natural-language summary. **If no API key is configured**,
     it falls back to a clear templated summary instead of failing the
     review.
3. **View results** (`GET /api/reviews/{id}`) — score, summary, and every
   finding, tagged with which tool produced it.
4. **Export** (`GET /api/reviews/{id}/report`) — OpenPDF-generated PDF with
   the score, summary, and a findings table.

## 4. Running it locally

### Backend
```bash
cd backend
# Point at a real Postgres (or Supabase) instance, or run one locally:
#   docker run -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres:16
export DB_URL=jdbc:postgresql://localhost:5432/codereview_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=change-this-super-secret-key-min-32-characters-long
export OPENAI_API_KEY=sk-...        # optional — falls back gracefully without it
mvn clean package
java -jar target/codereview.jar
# -> http://localhost:8080
```

### Frontend
```bash
cd frontend
cp .env .env
npm install
npm start
# -> http://localhost:3000
```

## 5. Deploying

- **Backend → Railway or Render**: `render.yaml` and `backend/Procfile` are
  included. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`,
  `OPENAI_API_KEY`, and `CORS_ORIGINS` (your Vercel URL) as environment
  variables on whichever platform you pick.
- **Frontend → Vercel**: import the `frontend/` folder, set
  `REACT_APP_API_BASE_URL` to your deployed backend's `/api` URL.
- **Database → Supabase or Railway Postgres**: either works — just plug the
  connection string into `DB_URL`.

## 6. API reference

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | — | Create account, returns JWT |
| POST | `/api/auth/login` | — | Log in, returns JWT |
| POST | `/api/projects/upload` | JWT | Upload `.java`/`.zip`, creates a project |
| GET | `/api/projects` | JWT | List your projects |
| GET | `/api/projects/{id}` | JWT | Get one project |
| POST | `/api/reviews/project/{projectId}` | JWT | Run static analysis + AI review |
| GET | `/api/reviews/{id}` | JWT | Get review + findings |
| GET | `/api/reviews/project/{projectId}` | JWT | List reviews for a project |
| GET | `/api/reviews/{id}/report` | JWT | Download PDF report |

## 7. Honest limitations worth knowing about

- **SpotBugs on arbitrary uploads**: bytecode analysis only runs when the
  uploaded code compiles standalone. A single class that imports an
  unresolved third-party library will legitimately fail to compile — that's
  surfaced as an `INFO` finding, not hidden or faked.
- **AI summary without an API key**: works, but produces a templated
  (non-LLM) summary rather than failing the review outright.
- **Single-node file storage**: uploads/reports are written to local disk
  (`uploads/`, `reports/`). On Railway/Render's ephemeral filesystem, treat
  the PDF as a "download it now" artifact rather than long-term storage —
  swapping in S3/Supabase Storage is a natural next step if you need that.
