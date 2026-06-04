package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
class RankingServiceFilterTest {

  @Autowired RankingService rankingService;

  @Autowired RankingRepository rankingRepository;

  private final LocalDate q = LocalDate.of(2026, 4, 1);
  private final LocalDate pq = LocalDate.of(2026, 1, 1);

  @BeforeEach
  void seed() {
    var now = LocalDateTime.now();
    rankingRepository.saveAll(
        List.of(
            r(10_001_001, "m00", q, 1, "500", false, false, "TC Baden", "BAD", now),
            r(10_002_002, "m00", q, 2, "490", false, false, "TC Other", "WTB", now),
            r(10_001_001, "m00", pq, 3, "460", false, false, "TC Baden", "BAD", now),
            r(20_001_001, "w00", q, 1, "500", false, false, "TC Baden", "BAD", now),
            r(10_711_111, "U14", q, 1, "100", true, false, "TC Baden", "BAD", now),
            r(10_711_112, "U13", q, 1, "90", false, true, "TC Baden", "BAD", now),
            r(10_001_001, "m00", LocalDate.of(2026, 1, 1), 1, "500", false, false, "TC Baden", "BAD", now, true)));
  }

  @AfterEach
  void cleanup() {
    rankingRepository.deleteAll();
  }

  @Test
  @DisplayName("adult male filter returns only male dtb_ids")
  void adultMaleFilterReturnsOnlyMaleDtbIds() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "m00", null, null, null, false), 1, 100);
    assertTrue(page.getContent().stream().allMatch(r -> r.dtbId() >= 10_000_000 && r.dtbId() <= 19_999_999));
  }

  @Test
  @DisplayName("adult female filter returns only female dtb_ids")
  void adultFemaleFilterReturnsOnlyFemaleDtbIds() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "w00", null, null, null, false), 1, 100);
    assertTrue(page.getContent().stream().allMatch(r -> r.dtbId() >= 20_000_000 && r.dtbId() <= 29_999_999));
  }

  @Test
  @DisplayName("U14 default filter selects ageGroupRanking=true")
  void u14DefaultFilterSelectsAgeGroupRankingTrue() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "mu14", null, null, null, false), 1, 100);
    assertTrue(page.getContent().stream().allMatch(Ranking::ageGroupRanking));
  }

  @Test
  @DisplayName("U13 odd default filter selects yobRanking=true")
  void u13OddDefaultFilterSelectsYobRankingTrue() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "mu13", null, null, null, false), 1, 100);
    assertTrue(page.getContent().stream().allMatch(Ranking::yobRanking));
  }

  @Test
  @DisplayName("only_yob option selects yobRanking=true")
  void onlyYobOptionSelectsYobRankingTrue() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "mu13", "only_yob", null, null, false), 1, 100);
    assertTrue(page.getContent().stream().allMatch(Ranking::yobRanking));
  }

  @Test
  @DisplayName("include_younger option selects yobRanking=false and ageGroupRanking=false")
  void includeYoungerOptionSelectsYobRankingFalseAndAgeGroupRankingFalse() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "m00", "include_younger", null, null, false), 1, 100);
    assertTrue(page.getContent().stream().allMatch(r -> !r.yobRanking() && !r.ageGroupRanking()));
  }

  @Test
  @DisplayName("federation filter limits results")
  void federationFilterLimitsResults() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "m00", null, "BAD", null, false), 1, 100);
    assertTrue(page.getContent().stream().allMatch(r -> r.federation().equals("BAD")));
  }

  @Test
  @DisplayName("club filter is case-insensitive")
  void clubFilterIsCaseInsensitive() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "m00", null, null, "tc baden", false), 1, 100);
    assertTrue(page.getContent().isEmpty() == false);
  }

  @Test
  @DisplayName("yearEnd=true on January quarter selects yearEndRanking=true")
  void yearEndTrueOnJanuaryQuarterSelectsYearEndRankingTrue() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(pq.toString(), "m00", null, null, null, true), 1, 100);
    assertTrue(page.getContent().stream().allMatch(Ranking::yearEndRanking));
  }

  @Test
  @DisplayName("yearEnd=true on non-January quarter selects yearEndRanking=false")
  void yearEndTrueOnNonJanuaryQuarterSelectsYearEndRankingFalse() {
    var page =
        rankingService.findFilteredRankings(
            new RankingFilter(q.toString(), "m00", null, null, null, true), 1, 100);
    assertTrue(page.getContent().stream().allMatch(r -> !r.yearEndRanking()));
  }

  @Test
  @DisplayName("fetchAvailableQuarters excludes Dec 31 year-end dates")
  void fetchAvailableQuartersExcludesDec31YearEndDates() {
    rankingRepository.save(
        r(10_001_001, "m00", LocalDate.of(2025, 12, 31), 1, "500", false, false));
    var quarters = rankingService.fetchAvailableQuarters();
    var allDates =
        quarters.values().stream()
            .flatMap(List::stream)
            .map(e -> e.second())
            .toList();
    assertFalse(allDates.contains("2025-12-31"));
  }

  @Test
  @DisplayName("findPreviousPositions returns empty map when no dtbIds")
  void findPreviousPositionsReturnsEmptyMapWhenNoDtbIds() {
    var result =
        rankingService.findPreviousPositions(
            new RankingFilter(q.toString(), "m00", null, null, null, false),
            List.of());
    assertTrue(result.isEmpty());
  }

  private Ranking r(
      int dtbId,
      String ageGroup,
      LocalDate date,
      int pos,
      String score,
      boolean ageGroupRanking,
      boolean yobRanking) {
    return r(dtbId, ageGroup, date, pos, score, ageGroupRanking, yobRanking, "TC Test", "TST", LocalDateTime.now(), false);
  }

  private Ranking r(
      int dtbId,
      String ageGroup,
      LocalDate date,
      int pos,
      String score,
      boolean ageGroupRanking,
      boolean yobRanking,
      String club,
      String federation,
      LocalDateTime now) {
    return r(dtbId, ageGroup, date, pos, score, ageGroupRanking, yobRanking, club, federation, now, false);
  }

  private Ranking r(
      int dtbId,
      String ageGroup,
      LocalDate date,
      int pos,
      String score,
      boolean ageGroupRanking,
      boolean yobRanking,
      String club,
      String federation,
      LocalDateTime now,
      boolean yearEndRanking) {
    return new Ranking(
        0L, dtbId, "Test", "Test", "GER", ageGroup, date, pos, score,
        club, federation, ageGroupRanking, yobRanking, yearEndRanking, now, now);
  }
}
