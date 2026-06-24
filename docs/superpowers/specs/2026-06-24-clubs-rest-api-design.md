# Clubs REST API — Design

## Goal

Expose the existing `/clubs` web functionality (club search and club roster detail) as a REST API
under `/api/v1/clubs`, following the same conventions already established by
`PlayersApiController` (bearer-token auth, `ProblemDetail` errors, `request`/`data` response
envelopes, snake_case JSON fields, `links.self` references to related resources).

## Scope

Two endpoints, mirroring `ClubController`'s two web routes:

- `GET /clubs` (search by name substring) → `GET /api/v1/clubs?name=...`
- `GET /clubs/{id}` (full roster for one club) → `GET /api/v1/clubs/{id}`

No changes to `ClubService`, `SecurityConfig`, or `RateLimitFilter` — both new routes fall under
the existing `/api/v1/**` bearer-token + rate-limit filter chain, and both delegate to the
existing `ClubService.searchClubs(String)` / `ClubService.getClubDetail(String)` methods without
modification.

## Endpoints

### `GET /api/v1/clubs?name={name}`

Search clubs whose name contains `name` (case-insensitive substring match), same semantics as
`ClubService.searchClubs`.

- `name` blank or missing → `400 Bad Request`, `ProblemDetail` with detail `"name parameter
  required"` (mirrors `PlayersApiController.search`'s `lastname` validation).
- No matching clubs → `404 Not Found`, `ProblemDetail` with detail `"Not Found"`.
- Otherwise → `200 OK` with:

```json
{
  "request": { "name": "TC", "total_count": 2 },
  "data": [
    { "name": "TC Example", "youth_count": 3, "adult_count": 7 },
    { "name": "TC Sample",  "youth_count": 0, "adult_count": 5 }
  ]
}
```

### `GET /api/v1/clubs/{id}`

Full roster for one club, identified by exact (case-insensitive) name match — same semantics as
`ClubService.getClubDetail`.

- No players found for `id` (unknown/misspelled club name) → `404 Not Found`.
- Otherwise → `200 OK` with the roster grouped by category, preserving the existing iteration
  order (`Herren`/`Damen` first, then `U12`, `U14`, `U16`, `U18` — whichever groups are
  non-empty):

```json
{
  "data": {
    "name": "TC Example",
    "groups": [
      {
        "group": "Herren",
        "players": [
          {
            "dtb_id": 10001001,
            "lastname": "Mustermann",
            "firstname": "Max",
            "rank": 1,
            "score": "1000",
            "links": { "self": "/api/v1/players/10001001" }
          }
        ]
      },
      { "group": "U14", "players": [ ... ] }
    ]
  }
}
```

## New types (package `de.hdawg.rankinginfo.api.v1`)

- `ClubsApiController` — `@RestController`, `@RequestMapping("/api/v1/clubs")`,
  `@SecurityRequirement(name = "bearerAuth")`. Two handler methods: `search` and `show`, structured
  like the corresponding methods in `PlayersApiController`.
- `ClubSearchItem(String name, int youth_count, int adult_count)`
- `ClubSearchRequest(String name, int total_count)`
- `ClubSearchResponse(ClubSearchRequest request, List<ClubSearchItem> data)` — defensive-copies
  `data` like `PlayerSearchResponse` does.
- `ClubDetailResponse(ClubDetailData data)`
- `ClubDetailData(String name, List<ClubGroup> groups)`
- `ClubGroup(String group, List<ClubPlayerItem> players)`
- `ClubPlayerItem(int dtb_id, String lastname, String firstname, int rank, String score,
  PlayerLink links)` — reuses the existing `PlayerLink(String self)` record.

`ClubsApiController.show` builds `groups` from the `LinkedHashMap<String, List<PlayerSummaryRow>>`
returned by `ClubService.getClubDetail`, preserving its key order. Each `PlayerSummaryRow` maps to
a `ClubPlayerItem` with `links.self = "/api/v1/players/" + dtbId`.

## Error handling

Both endpoints rely on the existing `GlobalApiExceptionHandler` for generic errors; `400`/`404`
responses are constructed explicitly in the controller, same as `PlayersApiController`.

## Testing

New `ClubsApiControllerTest` in `src/test/java/de/hdawg/rankinginfo/api/`, structured like
`PlayersApiControllerTest`:

- `search` returns `401` without a bearer token
- `show` returns `401` without a bearer token
- `search` returns `400` when `name` is missing/blank
- `search` returns `404` when no club matches
- `search` returns matching clubs with correct `youth_count`/`adult_count`
- `search` is case-insensitive
- `search` response contains `request` and `data` keys with required fields
- `show` returns `404` for an unknown club id
- `show` returns the roster for a known club, grouped correctly
- `show` preserves group order (adult categories before youth age groups, youth groups in
  `U12`/`U14`/`U16`/`U18` order)
- `show` player items contain required fields including `links.self`

Tests seed data via `RankingRepository` the same way `PlayersApiControllerTest` does, using the
H2 in-memory test database — no new fixtures or external services needed.

## Documentation

`README.md`'s "Endpoints" table (around line 107) lists every existing `/api/v1` route. Add two
rows for the new club endpoints, matching the existing style:

```
| `GET /api/v1/clubs?name=` | Club search by name (partial, case-insensitive) |
| `GET /api/v1/clubs/{id}` | Club roster grouped by category/age group |
```

## Out of scope

- No changes to the web `/clubs` controller, service, or templates.
- No new caching beyond what `RankingRepository`/`ClubService` already provide.
- No OpenAPI schema changes beyond the new `@Operation`/`@ApiResponse` annotations on the new
  controller methods (following the existing pattern in `PlayersApiController`).
