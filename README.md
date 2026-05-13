# AI Resume Builder

A full-stack AI-powered resume builder. Create, edit, and export professional resumes with AI-generated content, real-time suggestions, and ATS optimisation — all backed by OpenAI GPT-4o.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Run with Docker Compose](#run-with-docker-compose)
  - [Run Locally (without Docker)](#run-locally-without-docker)
- [API Overview](#api-overview)
- [Database Migrations](#database-migrations)
- [Architecture](#architecture)
- [Documentation](#documentation)

---

## Features

- **AI Resume Generation** — describe your experience and let GPT-4o write a structured resume
- **AI Improvement** — improve an existing resume with ATS scoring and keyword extraction
- **Bullet Point Enhancement** — rewrite individual bullets using the CAR (Challenge–Action–Result) framework
- **Resume Versioning** — every save creates an immutable snapshot; full version history preserved
- **PDF Export** — generate and download your resume as a PDF stored in S3
- **File Upload** — import existing resumes (PDF, DOCX, DOC, images)
- **Google & GitHub OAuth2** — social login alongside email/password
- **JWT Authentication** — stateless access + refresh token flow
- **Job Analysis** — analyse a job description against your resume for match score and suggestions
- **Drag-and-drop editor** — reorder resume sections with @dnd-kit

---

## Tech Stack

### Frontend
| | Technology |
|---|---|
| Framework | Next.js 15 (App Router, Turbopack) |
| Language | TypeScript 5 |
| Styling | Tailwind CSS 4, Radix UI, shadcn-style components |
| State | MobX 6 + TanStack React Query 5 |
| Forms | React Hook Form + Zod |
| HTTP | Axios |
| Drag & Drop | @dnd-kit |

### Backend
| | Technology |
|---|---|
| Framework | Spring Boot 3.3.4 |
| Language | Java 17 |
| Security | Spring Security, JWT (JJWT 0.12.3), OAuth2 |
| Database | PostgreSQL 16, Spring Data JPA, Hibernate |
| Migrations | Flyway |
| AI | OpenAI Chat Completions API (GPT-4o) via WebClient |
| Storage | AWS S3 (SDK v2) with pre-signed URLs |
| Mapping | MapStruct 1.5.5 |
| JSONB | Hypersistence Utils 3.7.3 |

### Infrastructure
| | Technology |
|---|---|
| Containerisation | Docker, Docker Compose |
| Database | PostgreSQL 16-Alpine |
| Build | Maven 3.9.6 (backend), npm (frontend) |

---

## Project Structure

```
aiResumeBuilder/
├── docker-compose.yml          # Orchestrates postgres, backend, frontend
├── .env.example                # Environment variable template
├── backend/
│   ├── Dockerfile              # 2-stage Maven → JRE build
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/app/
│       │   ├── ai/             # OpenAI client, prompts, parser, service
│       │   ├── auth/           # JWT, OAuth2, register/login endpoints
│       │   ├── common/         # ApiResponse, AppException, ErrorCode, BaseEntity
│       │   ├── config/         # Security, CORS, JPA auditing, Jackson
│       │   ├── file/           # S3 upload, PDF generation, signed URLs
│       │   ├── jobanalysis/    # Job description vs resume analysis
│       │   ├── resume/         # CRUD, soft delete, versioning
│       │   └── user/           # User entity, UserDetails, profile
│       └── resources/
│           ├── application.yml
│           └── db/migration/   # Flyway V1–V5
├── frontend/
│   ├── Dockerfile              # 3-stage deps → builder → runner
│   └── src/
│       ├── app/                # Next.js App Router (auth, dashboard, resume editor)
│       ├── components/         # Shared UI components
│       ├── modules/            # Feature modules (auth, resume, ai, job-analysis)
│       ├── services/           # API clients (auth, resume, ai)
│       ├── store/              # MobX stores (auth, resume, ui)
│       ├── types/              # TypeScript types
│       ├── utils/              # Helpers (cn, formatters, debounce)
│       └── validations/        # Zod schemas
└── docs/                       # Architecture & design PDFs
```

---

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (recommended)
- Or locally: Java 17+, Maven 3.9+, Node.js 22+, PostgreSQL 16

### Environment Variables

```bash
cp .env.example .env
```

Open `.env` and fill in the required values:

| Variable | Required | Description |
|---|---|---|
| `DB_PASSWORD` | Yes | PostgreSQL password |
| `JWT_SECRET` | Yes | 256-bit random string for signing JWTs |
| `OPENAI_API_KEY` | Yes | Your OpenAI API key (`sk-...`) |
| `GOOGLE_CLIENT_ID` | OAuth2 | Google Cloud OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | OAuth2 | Google Cloud OAuth2 client secret |
| `GITHUB_CLIENT_ID` | OAuth2 | GitHub OAuth app client ID |
| `GITHUB_CLIENT_SECRET` | OAuth2 | GitHub OAuth app client secret |
| `AWS_ACCESS_KEY_ID` | File upload | AWS IAM access key |
| `AWS_SECRET_ACCESS_KEY` | File upload | AWS IAM secret key |
| `AWS_S3_BUCKET` | File upload | S3 bucket name |

> OAuth2 and AWS vars are optional for initial local development — the app starts without them but those features will be unavailable.

### Run with Docker Compose

```bash
# Build all images and start postgres, backend, frontend
docker compose up --build

# Run in the background
docker compose up --build -d

# Tail logs
docker compose logs -f backend
docker compose logs -f frontend

# Stop everything (keeps the database volume)
docker compose down

# Stop and wipe the database
docker compose down -v
```

Service URLs once running:

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api/v1 |
| PostgreSQL | localhost:5432 |

**Startup order:** Docker waits for Postgres to pass its healthcheck before starting the backend. Flyway migrations run automatically on backend startup.

### Run Locally (without Docker)

**Backend:**

```bash
# Start a local PostgreSQL instance first, then:
cd backend
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="\
    -DDB_HOST=localhost \
    -DDB_PASSWORD=secret \
    -DJWT_SECRET=your-secret \
    -DOPENAI_API_KEY=sk-..."
```

**Frontend:**

```bash
cd frontend
npm install
cp .env.example .env.local   # set NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
npm run dev
```

---

## API Overview

All endpoints are prefixed with `/api/v1`. All responses follow the envelope:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2025-01-01T00:00:00Z"
}
```

### Auth — `/auth`
| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register with email & password |
| POST | `/auth/login` | Login, returns access + refresh tokens |
| POST | `/auth/refresh` | Exchange refresh token for new access token |
| POST | `/auth/logout` | Invalidate refresh token |
| GET | `/auth/me` | Current authenticated user |

### Resumes — `/resumes`
| Method | Path | Description |
|---|---|---|
| GET | `/resumes` | Paginated list of user's resumes |
| POST | `/resumes` | Create a new resume (creates version 1) |
| GET | `/resumes/{id}` | Get resume with latest version |
| PUT | `/resumes/{id}` | Update resume (creates new version) |
| DELETE | `/resumes/{id}` | Soft-delete resume |

### AI — `/ai`
| Method | Path | Description |
|---|---|---|
| POST | `/ai/generate` | Generate resume content from job title + skills |
| POST | `/ai/improve` | Improve resume with ATS scoring + keywords |
| POST | `/ai/improve-bullet` | Rewrite a single bullet (CAR framework) |

### Files — `/files`
| Method | Path | Description |
|---|---|---|
| POST | `/files/generate-pdf` | Generate PDF from latest resume version |
| POST | `/files/upload` | Upload a file (PDF, DOCX, DOC, image) |
| DELETE | `/files/{fileId}` | Delete file from S3 and database |

### Job Analysis — `/job-analyses`
| Method | Path | Description |
|---|---|---|
| POST | `/job-analyses` | Analyse a job description against a resume |
| GET | `/job-analyses/{id}` | Get analysis result |

---

## Database Migrations

Flyway manages the schema automatically on startup. Migration files are in `backend/src/main/resources/db/migration/`:

| Migration | Description |
|---|---|
| `V1__create_users.sql` | `users` table — email/password + OAuth2 provider fields |
| `V2__create_resumes.sql` | `resumes` table — soft delete, user FK |
| `V3__create_resume_versions.sql` | `resume_versions` — JSONB content snapshots, unique (resume_id, version_no) |
| `V4__create_job_analyses.sql` | `job_analyses` — JSONB suggestions, 0–100 match score check |
| `V5__create_file_metadata.sql` | `file_metadata` — S3 keys, nullable resume FK |

All tables share a `BaseEntity` with `id` (UUID), `created_at`, `updated_at`, `created_by`, `updated_by`.

---

## Architecture

```
Browser
  │
  ▼
Next.js Frontend (port 3000)
  │  REST / JSON over HTTPS
  ▼
Spring Boot Backend (port 8080)
  ├── Spring Security  ──── JWT filter + OAuth2 (Google, GitHub)
  ├── Resume Module    ──── CRUD + immutable version snapshots
  ├── AI Module        ──── WebClient → OpenAI GPT-4o
  ├── File Module      ──── AWS S3 upload + pre-signed GET URLs
  └── Job Analysis     ──── AI-powered JD vs resume scoring
        │
        ▼
  PostgreSQL 16
  (Flyway migrations, JSONB for resume content & AI suggestions)
```

**Key design decisions:**

- **Stateless JWT** — no server-side session; access token (1 day) + refresh token (7 days)
- **Immutable versioning** — resume content is append-only; `ResumeVersion` rows are never mutated
- **S3 key storage** — `file_metadata.file_url` stores the S3 object key, not a URL; signed URLs are generated on demand so they never expire in the database
- **JSONB for flexibility** — resume content schema evolves freely without migrations; stored as `Map<String, Object>`
- **OpenAI retry policy** — exponential backoff (1s → 8s, max 3 retries) on 5xx/network errors; 429 (rate limit) is not retried

---

## Documentation

Detailed design documents are in the `docs/` folder:

| Document | Contents |
|---|---|
| Product Overview | Feature list, user journeys, product goals |
| System Architecture | Component diagram, data flow, deployment topology |
| Development Setup | Local dev guide, tooling, environment setup |
| Frontend Architecture | Module structure, state management, routing |
| API Contracts | Full request/response schemas for every endpoint |
| Authentication & Authorization | JWT flow, OAuth2 flow, token lifecycle |
| Database Schema | ERD, table definitions, index strategy |
| AI Integration | Prompt design, retry strategy, response parsing |
