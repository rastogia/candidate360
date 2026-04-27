# Candidate 360 Profile System

A Spring Boot 3 application that builds 360-degree candidate profiles for recruiters by aggregating data from CVs, GitHub, LinkedIn, StackOverflow, Twitter, and AI enrichment.

## Architecture

```
CV Upload ──► CV Parser (PDFBox/POI)
                    │
Social Handles ─────┼──► Data Collectors (GitHub, SO, Twitter, LinkedIn, Web)
                    │
                    ▼
              Raw Source Data (PostgreSQL)
                    │
                    ▼
            AI Enrichment (Gemini / OpenAI)
                    │
                    ▼
          Scoring Engine + Red Flag Detector
                    │
                    ▼
            360° Profile + PDF Report
```

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local dev)
- Maven 3.9+

### Run with Docker
```bash
cp .env.example .env
# Edit .env with your API keys (all optional except JWT_SECRET)
docker-compose up -d
```

### Run Locally
```bash
# Start dependencies
docker-compose up -d postgres redis

# Run app
./mvnw spring-boot:run
```

### Access
- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Default login:** `admin@company.com` / `Admin@123`

## API Flow

1. **Login** → `POST /api/auth/login` → get JWT token
2. **Create candidate** → `POST /api/candidates`
3. **Upload CV** → `POST /api/candidates/{id}/cv`
4. **Add handles** → `POST /api/candidates/{id}/handles` (optional – auto-discovered from CV)
5. **Build 360 profile** → `POST /api/profiles` with `candidateId` + `jobRoleId`
6. **Check status** → `GET /api/profiles/{id}`
7. **Download PDF** → `GET /api/profiles/{id}/report`

## Data Points Collected

| Dimension | Sources | Data Points |
|-----------|---------|-------------|
| Technical Skills | GitHub, CV | Languages, repos, commits, stars |
| Community | StackOverflow | Reputation, answers, badges |
| Social Presence | Twitter, LinkedIn | Followers, bio, activity |
| Behavioural | AI Analysis | Communication, consistency, red flags |

## Configuration

All external API keys are optional. The system degrades gracefully:
- No AI key → Raw data summary returned
- No GitHub token → Unauthenticated (60 req/hr limit)
- No ProxyCurl key → LinkedIn collection skipped
- No Serper key → Web search / handle discovery skipped

## Tech Stack
- **Java 21** + **Spring Boot 3.2**
- **PostgreSQL 16** + **Flyway** migrations
- **Redis** for caching
- **PDFBox 3** + **Apache POI** for CV parsing
- **iText 7** for PDF report generation
- **WebFlux WebClient** for async API calls
- **JWT** authentication
- **SpringDoc OpenAPI** (Swagger UI)
