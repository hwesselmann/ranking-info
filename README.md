# ranking-info

[![test](https://github.com/hwesselmann/ranking-info/actions/workflows/test.yaml/badge.svg)](https://github.com/hwesselmann/ranking-info/actions/workflows/test.yaml)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=hwesselmann_ranking-info2&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=hwesselmann_ranking-info2)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hwesselmann_ranking-info2&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=hwesselmann_ranking-info2)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=hwesselmann_ranking-info2&metric=coverage)](https://sonarcloud.io/summary/new_code?id=hwesselmann_ranking-info2)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=hwesselmann_ranking-info2&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=hwesselmann_ranking-info2)

Java/Spring Boot reimplementation of [ranking-info](https://github.com/hwesselmann/ranking-info-rails) — a web application for browsing and analysing German national tennis youth and adult rankings.

## Tech stack

| Concern | Technology |
|---|---|
| Language / Framework | Java 25 / Spring Boot 4 |
| Database | H2 file-mode (dev), PostgreSQL (prod) |
| ORM | Spring Data JDBC |
| Frontend | jte (Java Template Engine) + Tailwind CSS |
| API docs | springdoc-openapi (Swagger UI at `/api-docs`) |
| Scheduling | `@Scheduled` (in-process) |
| Caching | Caffeine (in-memory) |
| Rate limiting | Bucket4j |
| Build | Maven (wrapper: `./mvnw`) |
| Tests | JUnit 5 + MockMvc + H2 |
| Coverage | JaCoCo |
| Static analysis | Spotless + PMD + SpotBugs |

## Prerequisites

- Java 25 (Eclipse Temurin or any distribution)
- Maven 3.9.16 or newer (wrapper included via `./mvnw` — local installation not required)
- Node.js + npm (for Tailwind CSS builds)
- Docker + Docker Compose (for containerised setup)

## Local development

```bash
# Install frontend dependencies (first time only)
npm install

# Build CSS (re-run after editing application.src.css or templates)
npx @tailwindcss/cli -i src/main/resources/static/css/application.src.css \
  -o src/main/resources/static/css/application.css

# Run with dev profile (H2 file-mode, auto-creates DB at ~/ranking-info-dev.mv.db)
./mvnw spring-boot:run
```

The application starts on [http://localhost:8080](http://localhost:8080).

## Local development with PostgreSQL

Use `docker-compose-local.yml` to run a PostgreSQL instance on `localhost:5432` and connect to it via the `local` Spring profile:

```bash
# Start PostgreSQL (data persisted in a named Docker volume)
docker compose -f docker-compose-local.yml up -d

# Run the app against it (import scheduler fires every 5 minutes)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Credentials: `ranking_info` / `changeme`. Liquibase applies all migrations (including trigram indexes) automatically on first startup.

## Tests

```bash
./mvnw test
```

The test suite uses an in-memory H2 database and the fixture CSV files in `src/test/resources/fixtures/`. No external services are needed.

## Linting

```bash
# Check (Spotless + PMD)
./mvnw spotless:check pmd:check

# Auto-fix Spotless violations
./mvnw spotless:apply
```

## Full build

```bash
# Compiles, tests, lints, coverage report, SpotBugs
./mvnw verify
```

## API

All API endpoints are under `/api/v1` and require Bearer token authentication.

### Authentication

Supply a token via:

- **Environment variable** `API_BEARER_TOKEN=your-token` (single token)
- **`application.yml`** under `api.tokens` (list of tokens)

Requests without a valid token receive `401 Unauthorized`. Exceeding 1000 requests/hour per token (or IP) returns `429 Too Many Requests`.

### Endpoints

| Method & Path | Description |
|---|---|
| `GET /api/v1/listings/{quarter}/{age_group_slug}` | Paginated ranking list. Slug: `m00` Herren, `w00` Damen, `mu18` Junioren U18, `wu12` Juniorinnen U12, etc. |
| `GET /api/v1/players?lastname=&yob=` | Player search by lastname (partial, case-insensitive) + optional year of birth |
| `GET /api/v1/players/{dtb_id}` | Player profile with full ranking history |

Swagger UI: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

#### Example

```bash
curl -H "Authorization: Bearer your-token" \
  "http://localhost:8080/api/v1/listings/2026-04-01/m00?per_page=10"
```

### ETag caching

All listing responses include an `ETag` header. Send `If-None-Match` with the ETag value to receive `304 Not Modified` when nothing has changed since the last import.

## Ranking import

Place CSV files in the import folder. Files are named `{Category}_{YYYYMMDD}.csv` where category is one of `Herren`, `Damen`, `Junioren`, or `Juniorinnen`.

### CSV format

```
ranking_position,lastname,firstname,nationality,dtb_id federation,club,score
```

This is the column order Tabula produces when converting the official DTB ranking PDFs.

### Configuration

| Variable / Property | Description | Default |
|---|---|---|
| `IMPORT_FOLDER` / `import.folder` | Folder scanned for CSV files | `~/ranking-import` |
| `import.schedule` | Cron expression | `0 0 12 * * ?` (daily at noon) |

The scheduler skips files already recorded in `import_histories`. Import errors are written to `error.log` in the import folder.

## Docker / Production

```bash
# Build and start
docker compose up --build

# Stop
docker compose down
```

The stack runs the application on port 8080 and PostgreSQL internally. Configure via environment variables:

| Variable | Description |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | PostgreSQL connection |
| `API_BEARER_TOKEN` | Bearer token for the API |
| `IMPORT_FOLDER` | Path inside the container for CSV imports |
| `DOMAIN` | Public hostname (shown on About page) |
| `IMPRINT_NAME`, `IMPRINT_STREET`, `IMPRINT_ZIPCODE`, `IMPRINT_CITY`, `IMPRINT_PHONE`, `IMPRINT_MAIL` | Imprint / legal notice |

## CI/CD

- **`test.yaml`** — runs `./mvnw verify` (tests, Spotless, PMD, SpotBugs, JaCoCo) and SonarCloud analysis on every push/PR
- **`codeql.yml`** — CodeQL security analysis on every push/PR and weekly
- **`docker.yaml`** — builds and pushes a Docker image to GHCR on every release
