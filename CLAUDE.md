# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Tools & Plugins

- **IntelliJ MCP** — Use the `mcp__idea__*` tools for all IDE-level operations: running the app, executing tests, reading files, searching symbols, renaming, and checking diagnostics. Prefer these over raw Bash/Maven calls whenever the MCP provides an equivalent action.
- **frontend-design plugin** — Use `/frontend-design` (or the plugin's skill) whenever editing HTML templates or CSS. It provides visual diffing and design context that plain file edits lack.

## Commits & Pull Requests

Do **not** add a `Co-Authored-By` trailer or any other AI-attribution line to commit messages or PR descriptions unless the user explicitly asks for it.

## After Every Code Change

Run all three steps in sequence after **any** change to Java, HTML, or CSS files:

```bash
# 1. Build & test
./mvnw test

# 2. Lint
./mvnw spotless:check pmd:check

# 3. If Spotless violations exist, apply and re-verify
./mvnw spotless:apply && ./mvnw spotless:check pmd:check
```

Stop and report failures before proceeding. Do not skip this sequence even for "trivial" changes.

## Commands

```bash
# Run locally (dev profile, H2 at ~/ranking-info-dev.mv.db)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest="de.hdawg.rankinginfo.service.ImportServiceTest"

# Lint (Spotless + PMD) — also runs as part of verify
./mvnw spotless:check pmd:check

# Auto-fix Spotless violations
./mvnw spotless:apply

# Full build (compiles, tests, lint, coverage, SpotBugs)
./mvnw verify
```

The build uses Maven with `spring-boot-starter-parent`. The app starts on `http://localhost:8080`. Swagger UI is at `/api-docs`.

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
| `dev` (default) | H2 file-mode at `~/ranking-info-dev.mv.db` |
| `prod` | PostgreSQL (via `DB_*` env vars) |
| `test` | H2 in-memory |

### Tests

Web controller tests extend `WebControllerTestBase`, which seeds a small set of `Ranking` rows via the real repository before each test and deletes them after. The `ImportIntegrationTest` runs the full import pipeline against H2 using fixture CSVs in `src/test/resources/fixtures/`.

CI (`test.yaml`) spins up a real PostgreSQL 16 container and runs `./mvnw verify` with `SPRING_PROFILES_ACTIVE=test`.
