# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Tools & Plugins

- **IntelliJ MCP** — Use the `mcp__idea__*` tools for all IDE-level operations: running the app, executing tests, reading files, searching symbols, renaming, and checking diagnostics. Prefer these over raw Bash/Gradle calls whenever the MCP provides an equivalent action.
- **frontend-design plugin** — Use `/frontend-design` (or the plugin's skill) whenever editing HTML templates or CSS. It provides visual diffing and design context that plain file edits lack.

## Commits & Pull Requests

Do **not** add a `Co-Authored-By` trailer or any other AI-attribution line to commit messages or PR descriptions unless the user explicitly asks for it.

## After Every Code Change

Run all three steps in sequence after **any** change to Kotlin, HTML, or CSS files:

```bash
# 1. Build & test
./gradlew test

# 2. Lint
./gradlew ktlintCheck detekt

# 3. If lint violations are auto-fixable, apply and re-verify
./gradlew ktlintFormat && ./gradlew ktlintCheck detekt
```

Stop and report failures before proceeding. Do not skip this sequence even for "trivial" changes.

## Commands

```bash
# Run locally (dev profile, SQLite at ~/ranking-info-dev.db)
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "de.hdawg.rankinginfo.service.ImportServiceTest"

# Lint (ktlint + detekt) — also runs as part of `check`
./gradlew ktlintCheck
./gradlew detekt

# Auto-fix ktlint violations
./gradlew ktlintFormat

# Full build (compiles, tests, lint, coverage)
./gradlew build

# Generate API docs (Dokka)
./gradlew dokkaGeneratePublicationHtml
```

The build uses Kotlin DSL (`build.gradle.kts`). Build caching and parallel execution are enabled in `gradle.properties`.

The app starts on `http://localhost:8080`. Swagger UI is at `/api-docs`.

## Architecture

Package root: `de.hdawg.rankinginfo`

```
domain/         JPA entities (Ranking, Player, Club, ImportHistory)
repository/     Spring Data JPA repositories + custom JPQL queries
service/        Business logic (ImportService, RankingService, PlayerService, RankingImportScheduler)
api/v1/         REST controllers (ListingsApiController, PlayersApiController) + ApiModels DTOs
api/security/   BearerTokenFilter, RateLimitFilter (Bucket4j, 1000 req/h)
web/            Thymeleaf MVC controllers (listing, player, club, federation, static pages)
config/         SecurityConfig (two filter chains), OpenApiConfig
```

### Data model

The single `Ranking` entity stores both raw import rows and all derived ranking rows for the same period. The `ageGroup` column distinguishes them:

| Value | Meaning |
|---|---|
| `m00` / `w00` | Adult men/women (Herren/Damen) |
| `overall` | Junior overall — the raw import row |
| `U12`, `U14`, `U16`, `U18` | Derived age-group rankings (even ages = official DTB age groups) |
| `U11`, `U13`, `U15`, `U17` | Derived YOB-only rankings |

`dtbId` encodes gender and year-of-birth: `1x_xxx_xxx` = male, `2x_xxx_xxx` = female. The first two significant digits after the gender prefix identify the year-of-birth class.

Three boolean flags distinguish ranking variants: `ageGroupRanking`, `yobRanking`, `yearEndRanking`. Year-end rankings are derived only when the import period month is January.

### Import flow

`RankingImportScheduler` calls `ImportService.scanAndImport()` on a cron schedule. `ImportService`:
1. Parses `<Category>_<yyyyMMdd>.csv` filenames (Herren/Damen/Junioren/Juniorinnen)
2. Stores raw rows with `ageGroup = "m00"/"w00"/"overall"`
3. For junior categories, calls `calculateRankings()` which derives U11–U18 rows by filtering `dtbId` ranges (via `yobRangeToFetch`)
4. Records the import in `ImportHistory` and evicts `available_quarters` + `federations` caches

### Security

Two Spring Security filter chains (`SecurityConfig`):
- **Order 1 — `/api/v1/**`**: `RateLimitFilter` (Bucket4j) → `BearerTokenFilter`. Tokens come from `api.tokens` config list and/or the `API_BEARER_TOKEN` env var.
- **Order 2 — everything else**: CSRF disabled, all requests permitted (no auth).

### Profiles

| Profile | Database |
|---|---|
| `dev` (default) | SQLite at `~/ranking-info-dev.db` |
| `prod` | PostgreSQL (via `DB_*` env vars) |
| `test` | H2 in-memory |

### Tests

Web controller tests extend `WebControllerTestBase`, which seeds a small set of `Ranking` rows via the real repository before each test and deletes them after. The `ImportIntegrationTest` runs the full import pipeline against H2 using fixture CSVs in `src/test/resources/fixtures/`.

CI (`test.yaml`) spins up a real PostgreSQL 16 container and runs `./gradlew build` with `SPRING_PROFILES_ACTIVE=test`.
