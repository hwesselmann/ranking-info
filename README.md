# ranking-info

[![test](https://github.com/hwesselmann/ranking-info/actions/workflows/test.yaml/badge.svg)](https://github.com/hwesselmann/ranking-info-kt/actions/workflows/test.yaml)

Kotlin/Spring Boot reimplementation of [ranking-info](https://github.com/hwesselmann/ranking-info-rails) — a web application for browsing and analysing German national tennis youth and adult rankings. All future development for ranking-info will take place in this project

## Tech stack

| Concern | Technology |
|---|---|
| Language / Framework | Kotlin 2 / Spring Boot 4 |
| Database | SQLite (dev), PostgreSQL (prod) |
| ORM / Migrations | Exposed (JetBrains) / Liquibase |
| Frontend | Thymeleaf + Tailwind CSS (CDN) |
| API docs | springdoc-openapi (Swagger UI at `/api-docs`) |
| Scheduling | `@Scheduled` (in-process) |
| Caching | Caffeine (in-memory) |
| Rate limiting | Bucket4j |
| Build | Gradle |
| Tests | JUnit 5 + MockMvc + H2 |
| Coverage | Kover |
| Static analysis | Detekt + ktlint |
| Documentation | Dokka |

## Prerequisites

- Java 21 (Eclipse Temurin or any distribution)
- Gradle wrapper included (`./gradlew`)
- Docker + Docker Compose (for containerised setup)

## Local development

```bash
# Run with dev profile (SQLite, auto-creates DB in $HOME/ranking-info-dev.db)
./gradlew bootRun
```

The application starts on [http://localhost:8080](http://localhost:8080).

## Tests

```bash
./gradlew test
```

The test suite uses an in-memory H2 database and the fixture CSV files in `src/test/resources/fixtures/`. No external services are needed.

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

- **`test.yaml`** — runs tests, ktlint, Detekt, and Dokka on every push/PR
- **`docker.yaml`** — builds and pushes a Docker image to GHCR on every release
