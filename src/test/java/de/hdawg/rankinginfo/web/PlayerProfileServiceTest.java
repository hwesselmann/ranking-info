package de.hdawg.rankinginfo.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.hdawg.rankinginfo.domain.Club;
import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.service.PlayerService;
import de.hdawg.rankinginfo.web.viewmodel.CompleteRankingRow;

class PlayerProfileServiceTest {

  private final PlayerProfileService service = new PlayerProfileService(null);

  private static Ranking r(int dtbId, String ageGroup, LocalDate date, int pos, String score) {
    return new Ranking(0L, dtbId, "Test", "Player", "GER", ageGroup, date, pos, score, "TC Test", "HH",
        false, false, false);
  }

  private static Ranking rClub(String club, String federation, LocalDate date) {
    return new Ranking(0L, 1, "Test", "Player", "GER", "m00", date, 5, "800", club, federation,
        false, false, false);
  }

  // --- buildClubs ---

  @Test
  @DisplayName("buildClubs orders clubs chronologically oldest to newest")
  void buildClubsChronologicalOrder() {
    var q1 = LocalDate.of(2022, 1, 1);
    var q2 = LocalDate.of(2023, 1, 1);
    var q3 = LocalDate.of(2024, 1, 1);
    // rawRankings is DateDesc
    var rankings = List.of(rClub("Club C", "HH", q3), rClub("Club B", "BY", q2), rClub("Club A", "NI", q1));
    var clubs = PlayerProfileService.buildClubs(rankings);
    assertEquals(List.of("Club A", "Club B", "Club C"), clubs.stream().map(Club::name).toList());
  }

  @Test
  @DisplayName("buildClubs deduplicates clubs, keeping oldest occurrence")
  void buildClubsDeduplication() {
    var q1 = LocalDate.of(2022, 1, 1);
    var q2 = LocalDate.of(2023, 1, 1);
    var q3 = LocalDate.of(2024, 1, 1);
    // Player was at TC Hamburg in 2022, moved to TC Berlin in 2023, returned to TC Hamburg in 2024
    var rankings = List.of(rClub("TC Hamburg", "HH", q3), rClub("TC Berlin", "BY", q2), rClub("TC Hamburg", "HH", q1));
    var clubs = PlayerProfileService.buildClubs(rankings);
    assertEquals(2, clubs.size());
    assertEquals("TC Hamburg", clubs.get(0).name());
    assertEquals("TC Berlin", clubs.get(1).name());
  }

  // --- buildCurrentRankings ---

  @Test
  @DisplayName("buildCurrentRankings returns empty list when currentQuarter is null")
  void buildCurrentRankingsNullQuarter() {
    var result = service.buildCurrentRankings(List.of(r(1, "m00", LocalDate.of(2024, 4, 1), 5, "800")), null, null);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("buildCurrentRankings with single quarter has null position/score change")
  void buildCurrentRankingsSingleQuarter() {
    var quarter = LocalDate.of(2024, 4, 1);
    var rankings = List.of(r(1, "m00", quarter, 5, "800"));
    var result = service.buildCurrentRankings(rankings, quarter, null);
    assertEquals(1, result.size());
    assertEquals("m00", result.get(0).ageGroup());
    assertEquals(5, result.get(0).position());
    assertNull(result.get(0).positionChange());
    assertNull(result.get(0).scoreChange());
  }

  @Test
  @DisplayName("buildCurrentRankings with two quarters computes position improvement")
  void buildCurrentRankingsTwoQuarters() {
    var prev = LocalDate.of(2024, 1, 1);
    var curr = LocalDate.of(2024, 4, 1);
    var rankings = List.of(
        r(1, "m00", curr, 3, "820"),
        r(1, "m00", prev, 5, "800"));
    var result = service.buildCurrentRankings(rankings, curr, prev);
    assertEquals(1, result.size());
    assertEquals("+2", result.get(0).positionChange());
  }

  @Test
  @DisplayName("buildCurrentRankings excludes overall age group")
  void buildCurrentRankingsExcludesOverall() {
    var quarter = LocalDate.of(2024, 4, 1);
    var rankings = List.of(
        r(1, "overall", quarter, 1, "850"),
        r(1, "m00", quarter, 5, "800"));
    var result = service.buildCurrentRankings(rankings, quarter, null);
    assertEquals(1, result.size());
    assertEquals("m00", result.get(0).ageGroup());
  }

  // --- buildCompleteRankings ---

  @Test
  @DisplayName("buildCompleteRankings groups by date and maps quarter correctly")
  void buildCompleteRankingsGrouping() {
    var rankings = List.of(
        r(1, "m00", LocalDate.of(2024, 4, 1), 5, "800"),
        r(1, "m00", LocalDate.of(2024, 1, 1), 6, "780"));
    var result = service.buildCompleteRankings(rankings);
    assertEquals(2, result.size());
    assertEquals("2024/Q2", result.get(0).date());
    assertEquals("2024/Q1", result.get(1).date());
  }

  @Test
  @DisplayName("buildCompleteRankings maps adult age groups to Aktive")
  void buildCompleteRankingsMapsAdultToAktive() {
    var rankings = List.of(r(1, "m00", LocalDate.of(2024, 4, 1), 5, "800"));
    var result = service.buildCompleteRankings(rankings);
    assertEquals(1, result.size());
    assertTrue(result.get(0).ageGroupPositions().containsKey("Aktive"));
  }

  @Test
  @DisplayName("buildCompleteRankings excludes ageGroupRanking and yobRanking rows")
  void buildCompleteRankingsFiltersFlags() {
    var date = LocalDate.of(2024, 4, 1);
    var general = new Ranking(0L, 1, "Test", "Player", "GER", "U14", date, 5, "800", "TC Test", "HH", false, false, false);
    var ageGroup = new Ranking(0L, 1, "Test", "Player", "GER", "U14", date, 3, "800", "TC Test", "HH", true, false, false);
    var yob = new Ranking(0L, 1, "Test", "Player", "GER", "U14", date, 2, "800", "TC Test", "HH", false, true, false);
    var result = service.buildCompleteRankings(List.of(general, ageGroup, yob));
    assertEquals(1, result.size());
    assertEquals(5, result.get(0).ageGroupPositions().get("U14"));
  }

  @Test
  @DisplayName("buildCompleteRankings skips rows with no QUARTER_MAP entry (e.g. month 2)")
  void buildCompleteRankingsSkipsUnknownMonth() {
    var rankings = List.of(r(1, "m00", LocalDate.of(2024, 2, 1), 5, "800"));
    var result = service.buildCompleteRankings(rankings);
    assertTrue(result.isEmpty());
  }

  // --- buildAvailableData ---

  @Test
  @DisplayName("buildAvailableData buckets years and quarters correctly")
  void buildAvailableData() {
    var rows = List.of(
        new CompleteRankingRow("2024/Q1", "800", Map.of()),
        new CompleteRankingRow("2024/Q2", "820", Map.of()),
        new CompleteRankingRow("2023/Q4", "750", Map.of()));
    var result = service.buildAvailableData(rows);
    assertEquals(2, result.size());
    assertTrue(result.get(2024).contains("Q1"));
    assertTrue(result.get(2024).contains("Q2"));
    assertTrue(result.get(2023).contains("Q4"));
  }

  @Test
  @DisplayName("buildAvailableData returns descending year order")
  void buildAvailableDataDescendingOrder() {
    var rows = List.of(
        new CompleteRankingRow("2022/Q1", "750", Map.of()),
        new CompleteRankingRow("2024/Q1", "800", Map.of()));
    var result = service.buildAvailableData(rows);
    var years = result.keySet().stream().toList();
    assertEquals(2024, years.get(0));
    assertEquals(2022, years.get(1));
  }

  // --- computeScoreChange (tested indirectly via buildCurrentRankings) ---

  @Test
  @DisplayName("score change is null when scores are equal")
  void scoreChangeNullWhenEqual() {
    var prev = LocalDate.of(2024, 1, 1);
    var curr = LocalDate.of(2024, 4, 1);
    var rankings = List.of(
        r(1, "m00", curr, 5, "800"),
        r(1, "m00", prev, 5, "800"));
    var result = service.buildCurrentRankings(rankings, curr, prev);
    assertNull(result.get(0).scoreChange());
  }

  @Test
  @DisplayName("score change handles comma-decimal format")
  void scoreChangeCommaDecimal() {
    var prev = LocalDate.of(2024, 1, 1);
    var curr = LocalDate.of(2024, 4, 1);
    var rankings = List.of(
        r(1, "m00", curr, 5, "66,9"),
        r(1, "m00", prev, 5, "64,0"));
    var result = service.buildCurrentRankings(rankings, curr, prev);
    assertTrue(result.get(0).scoreChange().startsWith("+"));
  }

  // --- PlayerService helpers ---

  @Test
  @DisplayName("dtbIdRange from 7-digit prefix returns correct range")
  void dtbIdRangeSevenDigit() {
    var service = new PlayerService(null);
    var range = service.dtbIdRange("1234567");
    assertEquals(12345670, range[0]);
    assertEquals(12345679, range[1]);
  }

  @Test
  @DisplayName("dtbIdRange from 8-digit ID returns exact range")
  void dtbIdRangeEightDigit() {
    var service = new PlayerService(null);
    var range = service.dtbIdRange("12345678");
    assertEquals(12345678, range[0]);
    assertEquals(12345678, range[1]);
  }

  @Test
  @DisplayName("dtbIdRange handles non-numeric input gracefully")
  void dtbIdRangeInvalid() {
    var service = new PlayerService(null);
    var range = service.dtbIdRange("ABC");
    assertEquals(0, range[0]);
  }

  @Test
  @DisplayName("yobToMaleMarker converts last two yob digits to male marker")
  void yobToMaleMarker() {
    var service = new PlayerService(null);
    assertEquals(110, service.yobToMaleMarker("2010"));
    assertEquals(100, service.yobToMaleMarker("2000"));
    assertEquals(112, service.yobToMaleMarker("2012"));
  }
}
