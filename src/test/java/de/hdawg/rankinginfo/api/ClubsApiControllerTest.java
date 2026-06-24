package de.hdawg.rankinginfo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ClubsApiControllerTest {

  @Autowired MockMvc mockMvc;

  @Autowired RankingRepository rankingRepository;

  @Autowired ImportHistoryRepository importHistoryRepository;

  private final String auth = "Bearer test-api-token";
  private final LocalDate q = LocalDate.of(2026, 4, 1);

  @BeforeEach
  void seed() {
    rankingRepository.saveAll(
        List.of(
            // Herren, TC Example
            r(10_001_001, "Mustermann", "Max", "m00", q, 1, "TC Example", false),
            // Damen, TC Example
            r(20_001_001, "Musterfrau", "Maxi", "w00", q, 1, "TC Example", false),
            // Herren, TC Sample (different club, should not match "Example" search)
            r(10_002_002, "Schmidt", "Peter", "m00", q, 1, "TC Sample", false),
            // Youth overall ranking, TC Example
            r(10_101_101, "Junior", "Jan", "overall", q, 5, "TC Example", false),
            // Age-group resolution for the youth player above (U14)
            r(10_101_101, "Junior", "Jan", "U14", q, 5, "TC Example", true)));
  }

  @AfterEach
  void cleanup() {
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  // ── Auth ─────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("search returns 401 without token")
  void searchReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/v1/clubs?name=Example")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("show returns 401 without token")
  void showReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/v1/clubs/TC Example")).andExpect(status().isUnauthorized());
  }

  // ── Search ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("search returns 400 when name missing")
  void searchReturns400WhenNameMissing() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs").header("Authorization", auth))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("name parameter required"));
  }

  @Test
  @DisplayName("search returns 404 when no club matches")
  void searchReturns404WhenNoClubMatches() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs?name=NichtVorhanden").header("Authorization", auth))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("search returns matching clubs by name")
  void searchReturnsMatchingClubsByName() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs?name=Example").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value("TC Example"));
  }

  @Test
  @DisplayName("search is case-insensitive")
  void searchIsCaseInsensitive() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs?name=example").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value("TC Example"));
  }

  @Test
  @DisplayName("search response contains request and data keys")
  void searchResponseContainsRequestAndDataKeys() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs?name=Example").header("Authorization", auth))
        .andExpect(jsonPath("$.request.name").value("Example"))
        .andExpect(jsonPath("$.request.total_count").isNumber())
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName("search data entry contains required fields")
  void searchDataEntryContainsRequiredFields() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs?name=Example").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].name").isString())
        .andExpect(jsonPath("$.data[0].youth_count").isNumber())
        .andExpect(jsonPath("$.data[0].adult_count").isNumber());
  }

  @Test
  @DisplayName("search counts youth and adult players correctly")
  void searchCountsYouthAndAdultPlayersCorrectly() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs?name=Example").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].youth_count").value(1))
        .andExpect(jsonPath("$.data[0].adult_count").value(2));
  }

  // ── Show ─────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("show returns 404 for unknown club")
  void showReturns404ForUnknownClub() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/Unknown Club").header("Authorization", auth))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("show returns roster grouped by category for known club")
  void showReturnsRosterGroupedByCategoryForKnownClub() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/TC Example").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("TC Example"))
        .andExpect(jsonPath("$.data.groups").isArray());
  }

  @Test
  @DisplayName("show preserves group order: adult categories before youth age groups")
  void showPreservesGroupOrder() throws Exception {
    // ClubService.ADULT_LABELS is a Map.of(...), so Herren/Damen relative order is not
    // guaranteed; only that both adult groups precede the youth U14 group is.
    mockMvc
        .perform(get("/api/v1/clubs/TC Example").header("Authorization", auth))
        .andExpect(jsonPath("$.data.groups.length()").value(3))
        .andExpect(jsonPath("$.data.groups[*].group").value(org.hamcrest.Matchers.hasItems("Herren", "Damen")))
        .andExpect(jsonPath("$.data.groups[2].group").value("U14"));
  }

  @Test
  @DisplayName("show player items contain required fields including links")
  void showPlayerItemsContainRequiredFields() throws Exception {
    mockMvc
        .perform(get("/api/v1/clubs/TC Example").header("Authorization", auth))
        .andExpect(jsonPath("$.data.groups[2].group").value("U14"))
        .andExpect(jsonPath("$.data.groups[2].players[0].dtb_id").value(10_101_101))
        .andExpect(jsonPath("$.data.groups[2].players[0].lastname").value("Junior"))
        .andExpect(jsonPath("$.data.groups[2].players[0].firstname").value("Jan"))
        .andExpect(jsonPath("$.data.groups[2].players[0].rank").value(5))
        .andExpect(jsonPath("$.data.groups[2].players[0].score").value("100"))
        .andExpect(
            jsonPath("$.data.groups[2].players[0].links.self").value("/api/v1/players/10101101"));
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private Ranking r(
      int dtbId,
      String lastname,
      String firstname,
      String ageGroup,
      LocalDate date,
      int pos,
      String club,
      boolean ageGroupRanking) {
    return new Ranking(
        0L, dtbId, lastname, firstname, "GER", ageGroup, date, pos, "100", club, "TST",
        ageGroupRanking, false, false);
  }
}
