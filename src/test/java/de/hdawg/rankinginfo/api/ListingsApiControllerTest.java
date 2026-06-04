package de.hdawg.rankinginfo.api;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
class ListingsApiControllerTest {

  @Autowired MockMvc mockMvc;

  @Autowired RankingRepository rankingRepository;

  @Autowired ImportHistoryRepository importHistoryRepository;

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

  private final String quarter = "2026-04-01";
  private final String prevQuarter = "2026-01-01";
  private final String auth = "Bearer test-api-token";

  @BeforeEach
  void seed() {
    var q = LocalDate.parse(quarter);
    var pq = LocalDate.parse(prevQuarter);
    var now = NOW;

    rankingRepository.saveAll(
        List.of(
            ranking(10_001_001, "Mueller", "Hans", "m00", q, 1, "500", false, false, now),
            ranking(10_002_002, "Schmidt", "Peter", "m00", q, 2, "490", false, false, now),
            ranking(10_003_003, "Wagner", "Karl", "m00", q, 3, "480", false, false, now),
            ranking(10_001_001, "Mueller", "Hans", "m00", pq, 3, "460", false, false, now),
            ranking(20_001_001, "Meyer", "Anna", "w00", q, 1, "500", false, false, now)));
  }

  @AfterEach
  void cleanup() {
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  // ── Authentication ───────────────────────────────────────────────────────

  @Test
  @DisplayName("returns 401 without authorization header")
  void returns401WithoutAuthorizationHeader() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("returns 401 with wrong token")
  void returns401WithWrongToken() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/listings/" + quarter + "/m00")
                .header("Authorization", "Bearer wrong-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("returns 200 with valid token")
  void returns200WithValidToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(status().isOk());
  }

  // ── Response structure ───────────────────────────────────────────────────

  @Test
  @DisplayName("response contains request and data keys")
  void responseContainsRequestAndDataKeys() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.request").exists())
        .andExpect(jsonPath("$.data").exists());
  }

  @Test
  @DisplayName("request block contains expected meta fields")
  void requestBlockContainsExpectedMetaFields() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(jsonPath("$.request.quarter").value(quarter))
        .andExpect(jsonPath("$.request.age_group").value("m00"))
        .andExpect(jsonPath("$.request.total_count").isNumber())
        .andExpect(jsonPath("$.request.page").value(1))
        .andExpect(jsonPath("$.request.per_page").value(25));
  }

  @Test
  @DisplayName("each data record contains required fields")
  void eachDataRecordContainsRequiredFields() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].dtb_id").isNumber())
        .andExpect(jsonPath("$.data[0].ranking_position").isNumber())
        .andExpect(jsonPath("$.data[0].lastname").isString())
        .andExpect(jsonPath("$.data[0].firstname").isString())
        .andExpect(jsonPath("$.data[0].score").isString())
        .andExpect(jsonPath("$.data[0].links.self").isString());
  }

  @Test
  @DisplayName("data is ordered by ranking_position ascending")
  void dataIsOrderedByRankingPositionAscending() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].ranking_position").value(1))
        .andExpect(jsonPath("$.data[1].ranking_position").value(2))
        .andExpect(jsonPath("$.data[2].ranking_position").value(3));
  }

  // ── Slug filtering ───────────────────────────────────────────────────────

  @Test
  @DisplayName("m00 slug returns only male dtb_ids")
  void m00SlugReturnsOnlyMaleDtbIds() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].dtb_id").value(10_001_001));
  }

  @Test
  @DisplayName("w00 slug returns only female dtb_ids")
  void w00SlugReturnsOnlyFemaleDtbIds() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/w00").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].dtb_id").value(20_001_001));
  }

  // ── Pagination ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("per_page limits result size")
  void perPageLimitsResultSize() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/listings/" + quarter + "/m00?per_page=1").header("Authorization", auth))
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.request.per_page").value(1));
  }

  @Test
  @DisplayName("per_page is capped at 100")
  void perPageIsCappedAt100() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/listings/" + quarter + "/m00?per_page=9999")
                .header("Authorization", auth))
        .andExpect(jsonPath("$.request.per_page").value(100));
  }

  @Test
  @DisplayName("total_count reflects all records regardless of page size")
  void totalCountReflectsAllRecordsRegardlessOfPageSize() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/listings/" + quarter + "/m00?per_page=1").header("Authorization", auth))
        .andExpect(jsonPath("$.request.total_count").value(3));
  }

  // ── Position change ──────────────────────────────────────────────────────

  @Test
  @DisplayName("position_change is calculated from previous quarter")
  void positionChangeIsCalculatedFromPreviousQuarter() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(jsonPath("$.data[0].dtb_id").value(10_001_001))
        .andExpect(jsonPath("$.data[0].position_change").value(2));
  }

  @Test
  @DisplayName("position_change is null for new entries without previous ranking")
  void positionChangeIsNullForNewEntriesWithoutPreviousRanking() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(jsonPath("$.data[1].dtb_id").value(10_002_002))
        .andExpect(jsonPath("$.data[1].position_change").value(nullValue()));
  }

  // ── ETag ─────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("response includes ETag header")
  void responseIncludesETagHeader() throws Exception {
    mockMvc
        .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
        .andExpect(header().exists("ETag"));
  }

  @Test
  @DisplayName("returns 304 when ETag matches")
  void returns304WhenETagMatches() throws Exception {
    var result =
        mockMvc
            .perform(get("/api/v1/listings/" + quarter + "/m00").header("Authorization", auth))
            .andReturn();

    var etag = result.getResponse().getHeader("ETag");

    mockMvc
        .perform(
            get("/api/v1/listings/" + quarter + "/m00")
                .header("Authorization", auth)
                .header("If-None-Match", etag))
        .andExpect(status().is(304));
  }

  @Test
  @DisplayName("returns 200 when ETag does not match")
  void returns200WhenETagDoesNotMatch() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/listings/" + quarter + "/m00")
                .header("Authorization", auth)
                .header("If-None-Match", "\"stale-etag\""))
        .andExpect(status().isOk());
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private Ranking ranking(
      int dtbId,
      String lastname,
      String firstname,
      String ageGroup,
      LocalDate date,
      int pos,
      String score,
      boolean ageGroupRanking,
      boolean yobRanking,
      LocalDateTime now) {
    return new Ranking(
        0L, dtbId, lastname, firstname, "GER", ageGroup, date, pos, score,
        "TC Test", "TST", ageGroupRanking, yobRanking, false, now, now);
  }
}
