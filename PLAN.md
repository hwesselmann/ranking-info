# Optimierungsplan ranking-info

Umsetzungsplan für die identifizierten Verbesserungen in den Bereichen Performance,
Architektur und Struktur. Die Maßnahmen sind in Phasen nach Wirkung/Aufwand sortiert.
Java 25 und Spring Boot 4 werden genutzt, wo es sinnvoll ist.

**Arbeitsweise nach jeder Phase:** `./mvnw test`, danach `./mvnw spotless:check pmd:check`
(bei Verstößen `./mvnw spotless:apply`). Vor dem Merge `./mvnw verify`.

**Commits, Pushes und Pull Requests** werden ausschließlich auf explizite Anforderung
erstellt — niemals automatisch durch Claude Code.

---

## Phase 1 — Kritisch / hoher Hebel ✅ abgeschlossen

### 1.1 Bare `ObjectMapper`-Bean entfernen ✅
**Problem:** `SecurityConfig` definiert `new ObjectMapper()` als Bean und deaktiviert damit
Boots vorkonfigurierten Mapper (keine Modulregistrierung, u. a. fehlt `JavaTimeModule`).
Die API serialisiert `LocalDate` (`RankingEntry`, `PlayerDetailData`) — latentes Bug-Risiko.

**Schritte:**
- [x] `objectMapper()`-Bean aus `config/SecurityConfig.java` entfernen.
- [x] In `apiSecurityFilterChain` den von Boot bereitgestellten `ObjectMapper` injizieren
      und an `BearerTokenFilter`/`RateLimitFilter` weiterreichen.
- [ ] Falls später Jackson-Anpassungen nötig: `Jackson2ObjectMapperBuilderCustomizer`
      statt eines eigenen Beans.

**Hinweis:** Spring Boot 4 verwendet Jackson 3 (`tools.jackson.*`). Alle Importe in
`SecurityConfig`, `BearerTokenFilter`, `RateLimitFilter`, `PlayerController` und
`BearerTokenFilterTest` wurden von `com.fasterxml.jackson.*` auf `tools.jackson.*`
migriert.

**Betroffen:** `config/SecurityConfig.java`, `api/security/BearerTokenFilter.java`,
`api/security/RateLimitFilter.java`, `web/PlayerController.java`

**Akzeptanz:** Test `showSerializesRankingQuarterAsIso8601String` in
`PlayersApiControllerTest` verifiziert ISO-8601-Serialisierung von `LocalDate`. ✅ Grün.

---

### 1.2 Virtuelle Threads aktivieren ✅
**Problem:** I/O-gebundene Last (DB + Template-Rendering), Tomcat-Pool in Prod manuell
auf 25 Threads getunt. Auf Java 25 entfällt das `synchronized`-Pinning (JEP 491).

**Schritte:**
- [x] `spring.threads.virtual.enabled=true` in `application.yml` setzen.
- [x] Manuelles `server.tomcat.threads.{max,min-spare}`-Tuning in `application-prod.yml`
      entfernt; Kommentar erklärt, dass HikariCP (`maximum-pool-size: 5`) der
      effektive Parallelitätsbegrenzer bleibt.
- [x] HikariCP-Pool-Größe bewusst bei 5 belassen (dokumentiert in `application-prod.yml`).

**Betroffen:** `application.yml`, `application-prod.yml`

**Akzeptanz:** App startet, Testsuite grün; kurzer Lasttest/Smoke-Test optional.

---

### 1.3 Fehlendes Caching ergänzen ✅
**Problem:** `maxImportedAt()` läuft pro API-Listing-Request (untergräbt ETag-Caching);
`FederationService.buildFederationData()` ist gar nicht gecacht (Doku nennt aber
`federation_stats`).

**Schritte:**
- [x] `RankingService.maxImportedAt()` mit `@Cacheable("max_imported_at")` versehen.
- [x] `FederationService.buildFederationData()` mit `@Cacheable("federation_stats")`
      versehen.
- [x] Beide Cache-Namen in alle drei `@CacheEvict`-Annotationen von `ImportService`
      aufgenommen (`importRankings`, `scanAndImport(String)`, `scanAndImport(String, String)`).

**Betroffen:** `service/RankingService.java`, `web/FederationService.java`,
`service/ImportService.java`

**Akzeptanz:** Testsuite grün. ✅ (Dedizierte Mock-Verify-Tests für Cache-Hits ausstehend,
können als Follow-up ergänzt werden.)

---

## Phase 2 — Architektur

### 2.1 Controller vom Repository entkoppeln
**Problem:** `PlayerController.show()` und `PlayersApiController` injizieren
`RankingRepository` direkt und orchestrieren Logik im Controller.

**Schritte:**
- [ ] Methode `PlayerProfileService.loadProfile(int dtbId)` (o. ä.) einführen, die
      Rohdaten, Diagrammdaten, „recent 4 dates" und View-Modelle gebündelt liefert.
- [ ] `PlayerController.show()` auf diese Service-Methode umstellen; leere Ergebnisse
      explizit behandeln statt `IndexOutOfBoundsException` zu fangen.
- [ ] `PlayersApiController` analog auf Service-Aufrufe umstellen, kein direktes
      Repository mehr.

**Betroffen:** `web/PlayerController.java`, `web/PlayerProfileService.java`,
`api/v1/PlayersApiController.java`, `service/PlayerService.java`

**Akzeptanz:** Controller hängen nicht mehr an `RankingRepository`; bestehende Web-/API-Tests grün.

---

### 2.2 Service-Pakete konsolidieren
**Problem:** Fachliche Services teils in `web` (`ClubService`, `FederationService`,
`PlayerProfileService`), teils in `service`.

**Schritte:**
- [ ] Entscheidung treffen: fachliche Services nach `service` verschieben (View-Mapping
      bleibt in `web`), **oder** die Aufteilung bewusst in `CLAUDE.md` dokumentieren.
- [ ] Bei Verschiebung: Paketpfade + Imports anpassen, Spotless-Importorder beachten.

**Betroffen:** `web/*Service.java`, ggf. `service/`, `CLAUDE.md`

**Akzeptanz:** Konsistente Layer-Struktur; `./mvnw verify` grün.

---

### 2.3 Ranking-Encoding zentralisieren
**Problem:** DTB-ID-Ranges, `genderFactor`, Slug↔AgeGroup-Logik und `* 100_000`-Rechnungen
sind über `RankingService`, `PlayerService`, `FederationService`, `ImportService` verstreut.

**Schritte:**
- [ ] Zentrale Utility/Value-Objects einführen (z. B. `RankingCoding` oder
      `DtbId` / `AgeGroupSlug`), die Ranges, Gender-Marker und Slug-Mapping kapseln.
- [ ] Aufrufer schrittweise migrieren; Duplikate entfernen.

**Betroffen:** `service/RankingService.java`, `service/PlayerService.java`,
`web/FederationService.java`, `service/ImportService.java` (neu: z. B. `domain/`)

**Akzeptanz:** Encoding-Logik an einer Stelle; Unit-Tests für die Kodierung; alle Tests grün.

---

### 2.4 `@CacheEvict`-Duplizierung beseitigen
**Problem:** Cache-Namen sind dreifach in `ImportService` dupliziert und weichen von der
Doku ab.

**Schritte:**
- [ ] Cache-Namen in einer Konstante bündeln oder eine `@CacheEvict`-Meta-Annotation
      erstellen, die alle relevanten Caches abdeckt.
- [ ] Auf alle Import-Methoden anwenden; `max_imported_at` und `federation_stats`
      (aus 1.3) einschließen.

**Betroffen:** `service/ImportService.java`

**Akzeptanz:** Eine einzige Quelle der Wahrheit für evictete Caches; Tests grün.

---

## Phase 3 — Sicherheit & Mikro-Optimierung

### 3.1 `BearerTokenFilter` härten
**Problem:** Pro Request wird ein neues `HashSet` aufgebaut; Token-Vergleich nicht
zeitkonstant (Timing-Seitenkanal).

**Schritte:**
- [ ] Gültige Tokens (konfiguriert + Env) einmal im Konstruktor zu einem unveränderlichen
      `Set<String>` zusammenführen.
- [ ] Vergleich über `MessageDigest.isEqual` gegen die Kandidaten (zeitkonstant).

**Betroffen:** `api/security/BearerTokenFilter.java`

**Akzeptanz:** Bestehende Security-Tests grün; ein Test für gültiges/ungültiges Token.

---

## Phase 4 — Moderne Sprach-/Framework-Features (Politur)

### 4.1 `SequencedCollection` (Java 21+)
- [ ] `get(0)` → `getFirst()`, `get(size()-1)` → `getLast()` in:
      `PlayerService.loadPlayerProfile()`, `ImportService.rankingsForAgeRange()`,
      `PlayerController.show()` (Datumsermittlung), weitere `findFirst`-Indizierungen.
- [ ] In `PlayerService.loadPlayerProfile()` Leerprüfung statt impliziter Exception.

**Akzeptanz:** Lesbarerer Code, identisches Verhalten, Tests grün.

### 4.2 `RestClient` statt `RestTemplate`
- [ ] `JteWarmup` von `RestTemplate` auf `RestClient` umstellen (RestTemplate ist im
      Maintenance-Modus).

**Betroffen:** `web/JteWarmup.java`

### 4.3 `@NullMarked` auf Paketebene
- [ ] `package-info.java` mit JSpecify-`@NullMarked` je Paket anlegen; redundante
      `@Nullable`-/Non-null-Annahmen bereinigen (Spring Framework 7 nutzt selbst JSpecify).

**Betroffen:** neue `package-info.java` je Paket

### 4.4 `ProblemDetail` (RFC 7807) für API-Fehler
- [ ] Eigenes `ErrorResponse` durch Springs `ProblemDetail` ersetzen
      (`type`/`title`/`status`/`detail`); Exception-Handler anpassen.

**Betroffen:** `api/v1/ErrorResponse.java`, `api/v1/GlobalApiExceptionHandler.java`,
`api/v1/*ApiController.java`

### 4.5 Kleinere Aufräumarbeiten
- [ ] Dependency-Classifier `bucket4j_jdk17-core` → `jdk21`-Variante prüfen (passend zur
      Toolchain).
- [ ] `ImportService.parseCsvLine` auf tatsächliche Nutzung prüfen, ggf. entfernen.
- [ ] `PlayerService.dtbIdRange()`: `Math.pow(10.0, …)` durch Integer-Logik/Lookup ersetzen
      (Floating-Point-Risiko vermeiden).

---

## Phase 5 — Optional / später abwägen

- [ ] **API-Versionierung** von manuellem `/api/v1` auf Boot-4-eigene API-Versionierung
      umstellen — erst sinnvoll, wenn eine v2 ansteht.
- [ ] **`MockMvcTester`** (AssertJ-basiert) für neue Web-Tests einführen.
- [ ] **Stream Gatherers (JDK 24)** — aktuell kein klarer Bedarf; `DISTINCT ON` in SQL
      bleibt überlegen.

---

## Reihenfolge / Empfehlung

1. 1.1 (ObjectMapper) — potenzieller Latent-Bug, zuerst absichern
2. 1.2 (Virtuelle Threads)
3. 1.3 (Caching) + 2.4 (Eviction-Dedupe gemeinsam)
4. 2.1 (Controller entkoppeln)
5. 3.1 (Token-Filter)
6. Phase 4 (Politur), 2.2/2.3 (Refactorings) und Phase 5 nach Bedarf

Jede Phase ist eigenständig mergebar (kleine, fokussierte PRs). Nach jeder Phase
Testsuite + Linting grün halten.
