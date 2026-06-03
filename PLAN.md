# Migration Plan: Kotlin → Java 25

## Ziel

Das Projekt vollständig von Kotlin auf Java 25 umstellen. Alle Kotlin-spezifischen
Abhängigkeiten (Exposed ORM, ktlint, detekt, kover, dokka, kotlin-stdlib) werden entfernt
und durch Java-Äquivalente ersetzt.

Entschiedene Eckpfeiler:
- **ORM**: Jetbrains Exposed → Spring Data JPA + Hibernate
- **Domain-Modell**: Kotlin data classes → Java Records
- **Qualitätssicherung**: ktlint/detekt/kover → Spotless + PMD/SpotBugs + JaCoCo (SonarQube bleibt)
- **Strategie**: Inkrementell, Schicht für Schicht; Kotlin und Java koexistieren bis Phase 10

---

## Phasenübersicht

| Phase | Inhalt | Kotlin-Dateien entfernt |
|-------|--------|------------------------|
| 1 | Build-Setup vorbereiten | — |
| 2 | Domain-Schicht | 4 |
| 3 | Persistence-Schicht (JPA Entities) | 2 |
| 4 | Repository-Schicht | 2 |
| 5 | Service-Schicht | 4 |
| 6 | Web-Controller | 5 |
| 7 | API-Controller & Security | 5 |
| 8 | Konfigurationsklassen | 4 |
| 9 | Tests | 13 |
| 10 | Kotlin-Toolchain entfernen | — |
| 11 _(optional)_ | Gradle → Maven | — |

---

## Phase 1: Build-Setup vorbereiten

**Ziel**: Beide Sprachen im selben Projekt kompilierbar machen; neue Qualitäts-Tools einrichten.

### 1.1 Neue Dependencies hinzufügen (build.gradle.kts)

```kotlin
// Ersetze exposed-spring-boot4-starter + exposed-java-time
implementation("org.springframework.boot:spring-boot-starter-data-jpa")

// JaCoCo (ersetzt Kover)
// via Gradle-Plugin

// Spotless
id("com.diffplug.spotless") version "7.x.x"

// PMD/SpotBugs
id("pmd")
id("com.github.spotbugs") version "6.x.x"
```

### 1.2 Spotless konfigurieren

```kotlin
spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
    }
}
```

### 1.3 JaCoCo + SonarQube verdrahten

```kotlin
// sonar-Block anpassen:
property("sonar.coverage.jacoco.xmlReportPaths",
    "build/reports/jacoco/test/jacobaTestReport.xml")
```

### 1.4 check-Task anpassen

```kotlin
tasks.named("check") {
    dependsOn("ktlintCheck", "detekt", "spotlessCheck", "pmdMain")
    // spotbugsMain wird erst in Phase 10 hinzugefügt (Java 25-Bytecode-Support)
}
```

**Hinweis SpotBugs**: SpotBugs 4.9.3 unterstützt Java-25-Bytecode (class file version 69) nicht.
`spotbugsMain`/`spotbugsTest` werden per `enabled = false` deaktiviert und erst in Phase 10
reaktiviert, sobald ein Update verfügbar ist und reiner Java-Code analysiert wird.

**Verifikation**: `./gradlew build` muss weiterhin grün sein (Kotlin-Code bleibt unberührt).

---

## Phase 2: Domain-Schicht (data classes → Records)

Dateien: `domain/Ranking.kt`, `domain/Player.kt`, `domain/Club.kt`, `domain/ImportHistory.kt`

### Migrationsmuster: Kotlin data class → Java Record

```kotlin
// Vorher (Kotlin)
data class Ranking(
    val id: Long = 0,
    val dtbId: Int = 0,
    val lastname: String = "",
    // ...
)
```

```java
// Nachher (Java 25)
public record Ranking(
    long id,
    int dtbId,
    String lastname,
    String firstname,
    String nationality,
    String ageGroup,
    LocalDate date,
    int rankingPosition,
    String score,
    String club,
    String federation,
    boolean ageGroupRanking,
    boolean yobRanking,
    boolean yearEndRanking,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /** Factory method: neues Ranking mit gesetzten Timestamps. */
    public static Ranking create(int dtbId, String ageGroup, LocalDate date, /* ... */) {
        var now = LocalDateTime.now();
        return new Ranking(0, dtbId, /* ... */, now, now);
    }
}
```

**Achtung zu Default-Werten**: Kotlin-Data-Classes haben Default-Parameter.
Java Records kennen keine. Stattdessen:
- Static Factory Methods für häufige Konstruktionsmuster
- Builder-Pattern nur wenn >5 Parameter optional sein müssen
- `Ranking.empty()` als Null-Objekt wo nötig

### Ebenfalls migrieren

- `RankingQueryFilter` (aktuell in `repository/RankingRepository.kt`) → eigene Datei `repository/RankingQueryFilter.java`
- `RankingFilter` (aktuell in `service/RankingService.kt`) → eigene Datei `service/RankingFilter.java`
- Alle 10 API-Modelle in `api/v1/ApiModels.kt` → Records in `api/v1/`

**Verifikation**: Kotlin-Schichten kompilieren noch gegen die neuen Java-Records.

---

## Phase 3: Persistence-Schicht (Exposed Entities → JPA Entities)

Dateien: `persistence/RankingsTable.kt`, `persistence/ImportHistoriesTable.kt`

### 3.1 JPA Entity für Ranking

```java
@Entity
@Table(name = "rankings")
public class RankingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dtb_id")
    private Integer dtbId;

    private String lastname;
    private String firstname;
    private String nationality;

    @Column(name = "age_group")
    private String ageGroup;

    private LocalDate date;

    @Column(name = "ranking_position")
    private Integer rankingPosition;

    private String score;
    private String club;
    private String federation;

    @Column(name = "age_group_ranking")
    private Boolean ageGroupRanking;

    @Column(name = "yob_ranking")
    private Boolean yobRanking;

    @Column(name = "year_end_ranking")
    private Boolean yearEndRanking;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Konvertiert zur unveränderlichen Domain-Record. */
    public Ranking toDomain() {
        return new Ranking(id, dtbId, lastname, /* ... */);
    }

    /** Befüllt Entity aus Domain-Record (ohne ID). */
    public static RankingEntity fromDomain(Ranking r) { /* ... */ }

    // Getter/Setter (oder Lombok @Data falls gewünscht; Records passen hier nicht,
    // da JPA mutable Entities erfordert)
}
```

**Hinweis**: JPA erfordert mutable Entities mit No-Arg-Konstruktor —
daher hier kein Record, sondern klassische Entity-Klasse.

### 3.2 JPA-Konfiguration

`application.yml` ergänzen:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none   # Liquibase übernimmt das Schema
    open-in-view: false
```

`application-dev.yml` — **H2 persistent (ersetzt SQLite)**:
```yaml
spring:
  datasource:
    url: jdbc:h2:file:${user.home}/ranking-info-dev;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

Datenbankdatei liegt unter `~/ranking-info-dev.mv.db` — Daten bleiben über Neustarts erhalten,
kein Neu-Import nötig. `MODE=PostgreSQL` stellt sicher, dass Liquibase-Changesets und SQL
unverändert funktionieren.

`application-prod.yml`:
```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

`application-test.yml` (unverändert, bleibt in-memory):
```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

### 3.3 SQLite-Abhängigkeiten entfernen

Da Dev-Profil auf H2 umgestellt wird:
- `SqliteDataSourceConfig.kt` **löschen** (der `setReadOnly()`-Wrapper entfällt komplett)
- `runtimeOnly("org.xerial:sqlite-jdbc:…")` aus `build.gradle.kts` entfernen
- `hibernate-community-dialects` wird **nicht** benötigt

Einzige H2-Dependency (bereits im Test-Scope vorhanden) in den `runtimeOnly`-Scope verschieben:
```kotlin
runtimeOnly("com.h2database:h2")  // war testRuntimeOnly, jetzt auch für dev
```

### 3.4 ExposedConfig.kt entfernen

Die Keyword-Casing-Anpassung war ein Exposed-spezifisches Workaround. Entfällt mit JPA.

**Verifikation**: `./gradlew test` mit `SPRING_PROFILES_ACTIVE=test`.

---

## Phase 4: Repository-Schicht (Exposed DSL → Spring Data JPA)

Dateien: `repository/RankingRepository.kt`, `repository/ImportHistoryRepository.kt`

### 4.1 Grundstruktur

```java
@Repository
public interface RankingRepository
        extends JpaRepository<RankingEntity, Long>, JpaSpecificationExecutor<RankingEntity> {

    // Einfache abgeleitete Queries
    List<RankingEntity> findByDtbIdAndAgeGroupInOrderByDateDesc(Integer dtbId, List<String> ageGroups);

    @Query("SELECT DISTINCT r.date FROM RankingEntity r WHERE r.date < :today ORDER BY r.date DESC")
    @Cacheable("available_dates")
    List<LocalDate> findDistinctDatesDesc(@Param("today") LocalDate today);

    // Gezählte DTB-IDs für Statusseite
    @Query("SELECT COUNT(DISTINCT r.dtbId) FROM RankingEntity r " +
           "WHERE r.dtbId BETWEEN :start AND :end")
    long countDistinctDtbIdInRange(@Param("start") int start, @Param("end") int end);
}
```

### 4.2 Dynamische WHERE-Klausel (findFiltered)

Die komplexe `buildWhere()`-Methode aus Exposed wird zur JPA-Specification:

```java
public class RankingSpecifications {

    public static Specification<RankingEntity> matchesFilter(RankingQueryFilter f) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("date"), f.date()));
            predicates.add(cb.equal(root.get("ageGroup"), f.ageGroup()));
            predicates.add(cb.between(root.get("dtbId"), f.dtbIdStart(), f.dtbIdEnd()));
            predicates.add(cb.equal(root.get("yobRanking"), f.yobRanking()));
            predicates.add(cb.equal(root.get("ageGroupRanking"), f.ageGroupRanking()));
            predicates.add(cb.equal(root.get("yearEndRanking"), f.yearEndRanking()));

            if (f.federation() != null)
                predicates.add(cb.equal(root.get("federation"), f.federation()));
            if (f.club() != null)
                predicates.add(cb.like(cb.lower(root.get("club")), "%" + f.club().toLowerCase() + "%"));
            if (f.dtbIds() != null)
                predicates.add(root.get("dtbId").in(f.dtbIds()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

Aufruf im Service:
```java
repository.findAll(RankingSpecifications.matchesFilter(filter), pageRequest);
```

### 4.3 Batch-Insert

Exposed's `batchInsert` → JPA `saveAll()` mit Hibernate-Batch-Config:
```yaml
spring.jpa.properties.hibernate.jdbc.batch_size: 50
spring.jpa.properties.hibernate.order_inserts: true
```

### 4.4 Transaktionsstrategie

Mit Spring Data JPA liegt `@Transactional` auf dem Service (nicht mehr auf dem Repository).
Die Repository-Klasse selbst bekommt kein `@Transactional` mehr.

**Verifikation**: Alle Repository-Tests grün; `ImportIntegrationTest` mit TestContainers.

---

## Phase 5: Service-Schicht

Dateien: `service/PlayerService.kt`, `service/RankingService.kt`,
`service/ImportService.kt`, `service/RankingImportScheduler.kt`

### Kotlin → Java Transformationsregeln

| Kotlin | Java 25 |
|--------|---------|
| `?.let { … }` | `if (x != null) { … }` |
| `?: fallback` | `x != null ? x : fallback` |
| `mapOf(…)` | `Map.of(…)` |
| `listOf(…)` | `List.of(…)` |
| `mutableListOf()` | `new ArrayList<>()` |
| `companion object { }` | `static` Felder/Methoden |
| `when (x) { … }` | `switch (x) { … }` (Java 21+ switch expression) |
| `.filter { }.map { }` | `.stream().filter().map().toList()` |
| `for (item in list) { }` | `for (var item : list) { }` |
| `"$variable text"` | `"text %s".formatted(variable)` oder Text Block |
| `require(cond) { msg }` | `if (!cond) throw new IllegalArgumentException(msg)` |
| `error(msg)` | `throw new IllegalStateException(msg)` |

### Beispiel: companion object → static

```kotlin
// Kotlin
companion object {
    private val SPECIAL_SCORES = setOf("0,0", "PR", "Einst.")
    private val CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

    fun extractPeriodFromFilename(filename: String): LocalDate { … }
}
```

```java
// Java 25
private static final Set<String> SPECIAL_SCORES = Set.of("0,0", "PR", "Einst.");
private static final Pattern CSV_SPLIT_PATTERN =
    Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

public static LocalDate extractPeriodFromFilename(String filename) { … }
```

### Beispiel: Nested Exception-Klasse

```kotlin
class DuplicateImportError(message: String) : Exception(message)
```

```java
public static final class DuplicateImportError extends Exception {
    public DuplicateImportError(String message) { super(message); }
}
```

**Verifikation**: `./gradlew test --tests "de.hdawg.rankinginfo.service.*"` grün.

---

## Phase 6: Web-Controller

Dateien: `web/PlayerController.kt`, `web/ListingController.kt`, `web/ClubController.kt`,
`web/FederationController.kt`, `web/StaticPageController.kt`

Spring MVC-Annotationen (`@Controller`, `@RequestMapping`, `@GetMapping`, etc.) bleiben identisch.
Thymeleaf-Templates müssen **nicht** geändert werden.

### Wichtige Transformationen

```kotlin
// Kotlin when
val slug = when (ageGroup) {
    "m00" -> "herren"
    "w00" -> "damen"
    else  -> ageGroup
}
```

```java
// Java switch expression (Java 21+)
var slug = switch (ageGroup) {
    case "m00" -> "herren";
    case "w00" -> "damen";
    default    -> ageGroup;
};
```

```kotlin
// Kotlin Map-Literal
val federationNames = mapOf(
    "BAD" to "Baden",
    "BTV" to "Bayern",
    // ...
)
```

```java
// Java (kann auch private static final Konstante sein)
private static final Map<String, String> FEDERATION_NAMES = Map.of(
    "BAD", "Baden",
    "BTV", "Bayern"
    // Map.of() max. 10 Einträge! Für mehr: Map.ofEntries(...)
);
```

**Verifikation**: `./gradlew test --tests "de.hdawg.rankinginfo.web.*"` grün.

---

## Phase 7: API-Controller & Security-Filter

Dateien: `api/v1/ListingsApiController.kt`, `api/v1/PlayersApiController.kt`,
`api/v1/ApiModels.kt`, `api/security/BearerTokenFilter.kt`, `api/security/RateLimitFilter.kt`

### ApiModels (10 Datenklassen → Records)

```kotlin
data class PlayerItem(
    val dtbId: String,
    val lastname: String,
    val firstname: String,
)
```

```java
public record PlayerItem(String dtbId, String lastname, String firstname) {}
```

Für verschachtelte Records (z.B. `PlayerDetailResponse` mit `PlayerData`):
→ Normale Java Records, keine besonderen Anforderungen.

### Filter-Klassen mit GenericFilterBean

```kotlin
class BearerTokenFilter(…) : GenericFilterBean() {
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) { … }
}
```

```java
public class BearerTokenFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException { … }
}
```

**Verifikation**: `./gradlew test --tests "de.hdawg.rankinginfo.api.*"` grün.

---

## Phase 8: Konfigurationsklassen

| Datei | Aktion |
|-------|--------|
| `config/SecurityConfig.kt` | → `SecurityConfig.java` |
| `config/ExposedConfig.kt` | **Löschen** (kein Exposed mehr) |
| `config/SqliteDataSourceConfig.kt` | → `SqliteDataSourceConfig.java` (nur Dev-Profil) |
| `config/OpenApiConfig.kt` | → `OpenApiConfig.java` |

### Spring Security Konfiguration bleibt strukturell gleich

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, …) throws Exception {
        return http
            .securityMatcher("/api/v1/**")
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
```

**Verifikation**: Applikation startet mit `./gradlew bootRun` und alle Seiten sind erreichbar.

---

## Phase 9: Tests

Dateien: Alle 13 Test-Kotlin-Dateien in `src/test/kotlin/`

### JUnit 5 Äquivalente

| Kotlin | Java |
|--------|------|
| `@Test` | `@Test` (identisch) |
| `kotlin.test.assertEquals(expected, actual)` | `Assertions.assertEquals(expected, actual)` |
| `kotlin.test.assertNotNull(x)` | `Assertions.assertNotNull(x)` |
| `kotlin.test.assertFails { }` | `Assertions.assertThrows(Exception.class, () -> { })` |
| `kotlin.test.assertTrue { }` | `Assertions.assertTrue(condition)` |
| Kotlin lambdas in MockMvc | Java lambdas (identisch) |

### WebControllerTestBase

```kotlin
// Kotlin abstract class
abstract class WebControllerTestBase {
    @Autowired lateinit var rankingRepository: RankingRepository
    // setup / teardown
}
```

```java
// Java abstract class
@SpringBootTest
@AutoConfigureMockMvc
public abstract class WebControllerTestBase {
    @Autowired
    protected RankingRepository rankingRepository;

    @BeforeEach
    void setUp() { /* Testdaten anlegen */ }

    @AfterEach
    void tearDown() { rankingRepository.deleteAll(); }
}
```

**Verifikation**: `./gradlew test` — alle Tests grün.

---

## Phase 10: Kotlin-Toolchain entfernen

Sobald **alle** Kotlin-Dateien durch Java ersetzt sind:

### build.gradle.kts bereinigen

```diff
 plugins {
-    kotlin("jvm") version "2.3.21"
-    kotlin("plugin.spring") version "2.3.21"
     id("org.springframework.boot") version "4.0.6"
     id("io.spring.dependency-management") version "1.1.7"
-    id("org.jetbrains.kotlinx.kover") version "0.9.8"
-    id("dev.detekt") version "2.0.0-alpha.3"
-    id("org.jetbrains.dokka") version "2.2.0"
-    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
     id("org.sonarqube") version "7.3.0.8198"
+    id("jacoco")
+    id("com.diffplug.spotless") version "7.x.x"
+    id("com.github.spotbugs") version "6.x.x"
 }
```

```diff
 dependencies {
-    implementation("org.jetbrains.kotlin:kotlin-reflect")
-    implementation("org.jetbrains.exposed:exposed-spring-boot4-starter:1.3.0")
-    implementation("org.jetbrains.exposed:exposed-java-time:1.3.0")
+    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
+    runtimeOnly("org.hibernate.orm:hibernate-community-dialects")  // SQLite-Dialekt

-    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
-    implementation(kotlin("stdlib"))
 }
```

### Weitere Aufräumarbeiten

- `detekt-baseline.xml` löschen
- `gradle/verification-metadata.xml` und `gradle/verification-keyring.keys` für neue Abhängigkeiten aktualisieren
- Dependency-Lock-Dateien neu generieren: `./gradlew dependencies --write-locks`
- `description` in build.gradle.kts: `"ranking-info-kt"` → `"ranking-info"`
- CI-Workflow überprüfen: Kotlin-spezifische Schritte entfernen (falls vorhanden)

**Abschlussverifikation**: `./gradlew build` — vollständiger Build ohne Kotlin-Compiler grün.

---

## Java 25 Features nutzen

Diese modernen Java-Features sollen aktiv eingesetzt werden:

| Feature | Einsatzbereich |
|---------|---------------|
| **Records** | Alle Domain-Objekte, DTOs, Filter-Objekte |
| **Sealed classes** | Fehlertypen: `sealed interface ImportError permits DuplicateImportError, ParseError` |
| **Switch expressions** | Überall wo `when` war (Controller, Services) |
| **Text Blocks** | Lange JPQL-Queries, SQL-Strings |
| **`var`** | Lokale Variablen mit offensichtlichem Typ |
| **`Stream` API** | Alle Collection-Transformationen |
| **`List.of()` / `Map.of()`** | Immutable Collections |
| **Pattern Matching für instanceof** | Typprüfungen in Controllern/Filtern |

---

## Risiken & offene Punkte

### R1: Dev-Profil auf H2 persistent umgestellt ✓
SQLite wird ersetzt durch H2 im File-Modus (`jdbc:h2:file:~/ranking-info-dev;MODE=PostgreSQL`).
Daten bleiben über Neustarts erhalten. `SqliteDataSourceConfig` und `sqlite-jdbc` entfallen.
H2 ist bereits im Projekt vorhanden (Test-Dependency) — nur Scope auf `runtimeOnly` erweitern.

### R2: JPA N+1-Problem
Exposed führt explizite Queries aus; JPA kann durch Lazy-Loading N+1-Probleme erzeugen.
→ Mitigierung: `@EntityGraph` oder `JOIN FETCH` in JPQL-Queries für häufige Lade-Muster.

### R3: Batch-Insert-Performance
Exposed's `batchInsert()` ist sehr effizient. JPA `saveAll()` batcht nur wenn
Hibernate-Batch-Config gesetzt ist. Unbedingt prüfen im `ImportIntegrationTest`.

### R4: Exposed Transaktionsmodell
Exposed verlangt eigene Transaktion um DB-Zugriffe. Spring Data JPA nutzt
Spring-Transaktionen. Der `@Transactional`-Scope verschiebt sich von Repository → Service.

### R5: Dependency-Lock-Dateien
Das Projekt nutzt Gradle Dependency Locking. Nach jeder Dependency-Änderung:
`./gradlew dependencies --write-locks` ausführen.

---

## Phase 11 (optional): Gradle → Maven

**Voraussetzung**: Phase 10 abgeschlossen — reines Java-Projekt, kein Kotlin mehr.

### 11.1 Neue pom.xml erstellen

Ausgangspunkt: Spring Initializr-generiertes POM mit denselben Abhängigkeiten.
Alle Dependency-Versionen aus `build.gradle.kts` übertragen.

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>de.hdawg.tennis</groupId>
  <artifactId>ranking-info</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>jar</packaging>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
  </parent>

  <properties>
    <java.version>25</java.version>
  </properties>

  <dependencies>
    <!-- identisch zu build.gradle.kts, übersetzt in Maven-Koordinaten -->
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

### 11.2 Qualitäts-Plugins in Maven

| Gradle-Plugin | Maven-Äquivalent |
|---------------|-----------------|
| Spotless | `spotless-maven-plugin` (com.diffplug.spotless) |
| SpotBugs | `spotbugs-maven-plugin` |
| PMD | `maven-pmd-plugin` |
| JaCoCo | `jacoco-maven-plugin` |
| SonarQube | `sonar-maven-plugin` |
| Liquibase | `liquibase-maven-plugin` (nur falls CLI-Nutzung nötig; Spring Boot führt Liquibase selbst aus) |

### 11.3 Dependency-Locking in Maven

Maven kennt kein natives Dependency-Locking. Äquivalente Absicherung:
- `maven-enforcer-plugin` mit `requireUpperBoundDeps` verhindert ungewollte Versionsauflösung
- Explizite Versionspins in `<dependencyManagement>` statt Lock-Dateien
- Optional: `maven-dependency-plugin:go-offline` für reproduzierbare Builds im CI

### 11.4 CI-Anpassung

Alle `./gradlew`-Aufrufe in `.github/workflows/` durch `./mvnw` (Maven Wrapper) ersetzen:

| Gradle | Maven |
|--------|-------|
| `./gradlew build` | `./mvnw verify` |
| `./gradlew test` | `./mvnw test` |
| `./gradlew bootRun` | `./mvnw spring-boot:run` |
| `./gradlew ktlintCheck detekt` | `./mvnw spotless:check spotbugs:check pmd:check` |

### 11.5 Aufräumen

- `build.gradle.kts` löschen
- `settings.gradle.kts` löschen
- `gradle/` Verzeichnis löschen (Wrapper, Verification-Metadata, Lock-Dateien)
- `gradlew` / `gradlew.bat` löschen
- Maven Wrapper generieren: `mvn wrapper:wrapper`
- `CLAUDE.md` aktualisieren: alle Gradle-Befehle durch Maven-Äquivalente ersetzen

### 11.6 Was Maven nicht kann (Gradle-Features die entfallen)

| Gradle-Feature | Status in Maven |
|----------------|----------------|
| Inkrementeller Compiler | Nicht verfügbar; Maven kompiliert immer vollständig |
| Build-Cache (`--build-cache`) | Nicht verfügbar nativ (Enterprise-Feature via Develocity) |
| Parallele Subprojekt-Builds (`--parallel`) | Nur bei Multi-Modul-Projekten relevant; hier: Mono-Repo → kein Unterschied |
| Dependency Locking | Kein direktes Äquivalent (siehe 11.3) |

Für dieses Projekt (Single-Module, ~30 Dateien) sind die Performance-Unterschiede vernachlässigbar.

**Verifikation**: `./mvnw verify` — vollständiger Build inklusive Tests, Linting und Coverage grün.
