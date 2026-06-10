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
class PlayersApiControllerTest {

  @Autowired MockMvc mockMvc;

  @Autowired RankingRepository rankingRepository;

  @Autowired ImportHistoryRepository importHistoryRepository;

  private final String auth = "Bearer test-api-token";
  private final LocalDate q = LocalDate.of(2026, 4, 1);

  @BeforeEach
  void seed() {
    rankingRepository.saveAll(
        List.of(
            r(10_001_001, "Mustermann", "Max", "m00", q, 1, false, false),
            r(10_001_001, "Mustermann", "Max", "m00", LocalDate.of(2026, 1, 1), 3, false, false),
            r(10_002_002, "Schmidt", "Peter", "m00", q, 2, false, false)));
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
    mockMvc
        .perform(get("/api/v1/players?lastname=Mustermann"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("show returns 401 without token")
  void showReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/v1/players/10001001")).andExpect(status().isUnauthorized());
  }

  // ── Search ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("search returns 400 when lastname missing")
  void searchReturns400WhenLastnameMissing() throws Exception {
    mockMvc
        .perform(get("/api/v1/players").header("Authorization", auth))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("lastname parameter required"));
  }

  @Test
  @DisplayName("search returns 404 when no player matches")
  void searchReturns404WhenNoPlayerMatches() throws Exception {
    mockMvc
        .perform(get("/api/v1/players?lastname=NichtVorhanden").header("Authorization", auth))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("search returns matching players by lastname")
  void searchReturnsMatchingPlayersByLastname() throws Exception {
    mockMvc
        .perform(get("/api/v1/players?lastname=Mustermann").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].dtb_id").value(10_001_001))
        .andExpect(jsonPath("$.data[0].lastname").value("Mustermann"));
  }

  @Test
  @DisplayName("search is case-insensitive")
  void searchIsCaseInsensitive() throws Exception {
    mockMvc
        .perform(get("/api/v1/players?lastname=mustermann").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].lastname").value("Mustermann"));
  }

  @Test
  @DisplayName("search response contains request and data keys")
  void searchResponseContainsRequestAndDataKeys() throws Exception {
    mockMvc
        .perform(get("/api/v1/players?lastname=Mustermann").header("Authorization", auth))
        .andExpect(jsonPath("$.request.lastname").value("Mustermann"))
        .andExpect(jsonPath("$.request.total_count").isNumber())
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName("search data entry contains required fields")
  void searchDataEntryContainsRequiredFields() throws Exception {
    mockMvc
        .perform(get("/api/v1/players?lastname=Mustermann").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].dtb_id").isNumber())
        .andExpect(jsonPath("$.data[0].lastname").isString())
        .andExpect(jsonPath("$.data[0].firstname").isString())
        .andExpect(jsonPath("$.data[0].club").isString())
        .andExpect(jsonPath("$.data[0].links.self").isString());
  }

  @Test
  @DisplayName("search self link points to player detail")
  void searchSelfLinkPointsToPlayerDetail() throws Exception {
    mockMvc
        .perform(get("/api/v1/players?lastname=Mustermann").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].links.self").value("/api/v1/players/10001001"));
  }

  @Test
  @DisplayName("search filters by yob when provided")
  void searchFiltersByYobWhenProvided() throws Exception {
    mockMvc
        .perform(get("/api/v1/players?lastname=Mustermann&yob=2000").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].dtb_id").value(10_001_001));
  }

  // ── Show ─────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("show returns 404 for unknown dtb_id")
  void showReturns404ForUnknownDtbId() throws Exception {
    mockMvc
        .perform(get("/api/v1/players/99999999").header("Authorization", auth))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("show returns player detail for known dtb_id")
  void showReturnsPlayerDetailForKnownDtbId() throws Exception {
    mockMvc
        .perform(get("/api/v1/players/10001001").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.dtb_id").value(10_001_001))
        .andExpect(jsonPath("$.data.lastname").value("Mustermann"))
        .andExpect(jsonPath("$.data.firstname").value("Max"));
  }

  @Test
  @DisplayName("show returns required fields")
  void showReturnsRequiredFields() throws Exception {
    mockMvc
        .perform(get("/api/v1/players/10001001").header("Authorization", auth))
        .andExpect(jsonPath("$.data.dtb_id").isNumber())
        .andExpect(jsonPath("$.data.lastname").isString())
        .andExpect(jsonPath("$.data.firstname").isString())
        .andExpect(jsonPath("$.data.nationality").isString())
        .andExpect(jsonPath("$.data.club").isString())
        .andExpect(jsonPath("$.data.federation").isString())
        .andExpect(jsonPath("$.data.rankings").isArray());
  }

  @Test
  @DisplayName("show rankings contain required fields")
  void showRankingsContainRequiredFields() throws Exception {
    mockMvc
        .perform(get("/api/v1/players/10001001").header("Authorization", auth))
        .andExpect(jsonPath("$.data.rankings[0].quarter").isString())
        .andExpect(jsonPath("$.data.rankings[0].age_group").isString())
        .andExpect(jsonPath("$.data.rankings[0].ranking_position").isNumber())
        .andExpect(jsonPath("$.data.rankings[0].score").isString());
  }

  @Test
  @DisplayName("show serializes ranking quarter as ISO-8601 string")
  void showSerializesRankingQuarterAsIso8601String() throws Exception {
    mockMvc
        .perform(get("/api/v1/players/10001001").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.rankings[0].quarter").value("2026-04-01"));
  }

  @Test
  @DisplayName("show returns multiple ranking entries across quarters")
  void showReturnsMultipleRankingEntriesAcrossQuarters() throws Exception {
    mockMvc
        .perform(get("/api/v1/players/10001001").header("Authorization", auth))
        .andExpect(jsonPath("$.data.rankings.length()").value(2));
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private Ranking r(
      int dtbId,
      String lastname,
      String firstname,
      String ageGroup,
      LocalDate date,
      int pos,
      boolean ageGroupRanking,
      boolean yobRanking) {
    return new Ranking(
        0L, dtbId, lastname, firstname, "GER", ageGroup, date, pos, "100",
        "TC Test", "TST", ageGroupRanking, yobRanking, false);
  }
}
