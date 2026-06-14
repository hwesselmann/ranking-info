package de.hdawg.rankinginfo.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.hdawg.rankinginfo.domain.Club;
import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.service.PlayerService;
import de.hdawg.rankinginfo.web.viewmodel.CompleteRankingRow;

class PlayerProfileServiceTest {

  private final PlayerProfileService service = new PlayerProfileService(null);

  private static Ranking r(int dtbId, String ageGroup, LocalDate date, int pos, String score) {
    return new Ranking(0L, dtbId, "Test", "Player", "GER", ageGroup, date, pos, score, "TC Test",
        "HH",
        false, false, false);
  }

  private static Ranking rClub(String club, String federation, LocalDate date) {
    return new Ranking(0L, 1, "Test", "Player", "GER", "m00", date, 5, "800", club, federation,
        false, false, false);
  }

  // --- buildClubs ---

  @Test
  @DisplayName("buildClubs orders clubs newest to oldest")
  void buildClubsChronologicalOrder() {
    var q1 = LocalDate.of(2022, Month.JANUARY, 1);
    var q2 = LocalDate.of(2023, Month.JANUARY, 1);
    var q3 = LocalDate.of(2024, Month.JANUARY, 1);
    // rawRankings is DateDesc
    var rankings = List.of(rClub("Club C", "HH", q3), rClub("Club B", "BY", q2),
        rClub("Club A", "NI", q1));
    var clubs = PlayerProfileService.buildClubs(rankings);
    assertEquals(List.of("Club C", "Club B", "Club A"), clubs.stream().map(Club::name).toList());
  }

  @Test
  @DisplayName("buildClubs deduplicates clubs, keeping most recent occurrence first")
  void buildClubsDeduplication() {
    var q1 = LocalDate.of(2022, Month.JANUARY, 1);
    var q2 = LocalDate.of(2023, Month.JANUARY, 1);
    var q3 = LocalDate.of(2024, Month.JANUARY, 1);
    // Player was at TC Hamburg in 2022, moved to TC Berlin in 2023, returned to TC Hamburg in 2024
    var rankings = List.of(rClub("TC Hamburg", "HH", q3), rClub("TC Berlin", "BY", q2),
        rClub("TC Hamburg", "HH", q1));
    var clubs = PlayerProfileService.buildClubs(rankings);
    assertEquals(2, clubs.size());
    assertEquals("TC Hamburg", clubs.get(0).name());
    assertEquals("TC Berlin", clubs.get(1).name());
  }

  // --- buildCurrentRankings ---

  // dtbId=1 is treated as adult (isJunior=false) because birthYear decodes to age >> 18
  private static final int ADULT_DTB_ID = 1;
  // Boy born 2010: marker=110, dtbId=11_000_001 → age 14 in Q2/2024, double cohort U14
  private static final int JUNIOR_DTB_ID_2010 = 11_000_001;

  @Test
  @DisplayName("buildCurrentRankings returns empty list when currentQuarter is null")
  void buildCurrentRankingsNullQuarter() {
    var result = service.buildCurrentRankings(ADULT_DTB_ID,
        List.of(r(ADULT_DTB_ID, "m00", LocalDate.of(2024, Month.APRIL, 1), 5, "800")), null, null,
        List.of());
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("buildCurrentRankings with single quarter has null position/score change")
  void buildCurrentRankingsSingleQuarter() {
    var quarter = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(r(ADULT_DTB_ID, "m00", quarter, 5, "800"));
    var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, quarter, null, List.of());
    assertEquals(1, result.size());
    assertEquals("m00", result.getFirst().ageGroup());
    assertEquals(5, result.getFirst().position());
    assertNull(result.getFirst().positionChange());
    assertNull(result.getFirst().scoreChange());
  }

  @Test
  @DisplayName("buildCurrentRankings with two quarters computes position improvement")
  void buildCurrentRankingsTwoQuarters() {
    var prev = LocalDate.of(2024, Month.JANUARY, 1);
    var curr = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        r(ADULT_DTB_ID, "m00", curr, 3, "820"),
        r(ADULT_DTB_ID, "m00", prev, 5, "800"));
    var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, curr, prev, List.of());
    assertEquals(1, result.size());
    assertEquals("+2", result.getFirst().positionChange());
  }

  @Test
  @DisplayName("buildCurrentRankings: prev position matched by ageGroup, not just date")
  void buildCurrentRankingsPrevMatchedByAgeGroup() {
    var prev = LocalDate.of(2024, Month.JANUARY, 1);
    var curr = LocalDate.of(2024, Month.APRIL, 1);
    // U16 in prev must come before U14 so the lambda evaluates the mismatch branch
    // before findFirst() short-circuits on the matching U14 entry
    var rankings = List.of(
        r(JUNIOR_DTB_ID_2010, "U14", curr, 8, "750"),
        r(JUNIOR_DTB_ID_2010, "U16", prev, 25, "720"),
        r(JUNIOR_DTB_ID_2010, "U14", prev, 12, "720"));
    var result = service.buildCurrentRankings(JUNIOR_DTB_ID_2010, rankings, curr, prev, List.of());
    var u14 = result.stream().filter(r -> "U14".equals(r.ageGroup())).findFirst().orElseThrow();
    assertEquals("+4", u14.positionChange());
  }

  @Test
  @DisplayName("buildCurrentRankings excludes overall age group")
  void buildCurrentRankingsExcludesOverall() {
    var quarter = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        r(ADULT_DTB_ID, "overall", quarter, 1, "850"),
        r(ADULT_DTB_ID, "m00", quarter, 5, "800"));
    var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, quarter, null, List.of());
    assertEquals(1, result.size());
    assertEquals("m00", result.getFirst().ageGroup());
  }

  @Test
  @DisplayName("buildCurrentRankings: adult rows have showScore=true, isCurrentAgeGroup=false")
  void buildCurrentRankingsAdultFlags() {
    var quarter = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(r(ADULT_DTB_ID, "m00", quarter, 5, "800"));
    var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, quarter, null, List.of());
    assertEquals(1, result.size());
    assertTrue(result.getFirst().showScore());
    assertFalse(result.getFirst().isCurrentAgeGroup());
    assertNull(result.getFirst().ageGroupRankingPosition());
  }

  @Test
  @DisplayName("buildCurrentRankings: current single age group is highlighted, others show no score")
  void buildCurrentRankingsJuniorFlags() {
    // Boy born 2010 → U14 single, U14 double in Q2/2024
    var quarter = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        r(JUNIOR_DTB_ID_2010, "U14", quarter, 8, "750"),
        r(JUNIOR_DTB_ID_2010, "U16", quarter, 20, "750"),
        r(JUNIOR_DTB_ID_2010, "U18", quarter, 35, "750"));
    var result = service.buildCurrentRankings(JUNIOR_DTB_ID_2010, rankings, quarter, null,
        List.of());
    // First row should be U14 (current single), highlighted with showScore=true
    assertEquals("U14", result.getFirst().ageGroup());
    assertTrue(result.getFirst().isCurrentAgeGroup());
    assertTrue(result.getFirst().showScore());
    // Other rows: showScore=false
    assertFalse(result.get(1).showScore());
    assertFalse(result.get(2).showScore());
  }

  @Test
  @DisplayName("buildCurrentRankings: ageGroupRankingPosition attached to double cohort row")
  void buildCurrentRankingsAgeGroupRankingPosition() {
    // Boy born 2010 → U14 double cohort in Q2/2024
    var quarter = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(r(JUNIOR_DTB_ID_2010, "U14", quarter, 8, "750"));
    var ageGroupEntry = new Ranking(0L, JUNIOR_DTB_ID_2010, "Test", "Player", "GER", "U14", quarter,
        3, "750", "TC Test", "HH", true, false, false);
    var result = service.buildCurrentRankings(JUNIOR_DTB_ID_2010, rankings, quarter, null,
        List.of(ageGroupEntry));
    assertEquals(1, result.size());
    assertEquals(3, result.getFirst().ageGroupRankingPosition());
  }

  @Test
  @DisplayName("buildCurrentRankings: junior rows sorted with current single first, then ascending U-number")
  void buildCurrentRankingsJuniorSortOrder() {
    // Boy born 2010 → U14 single in Q2/2024; also has U16, U18
    var quarter = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        r(JUNIOR_DTB_ID_2010, "U18", quarter, 35, "750"),
        r(JUNIOR_DTB_ID_2010, "U14", quarter, 8, "750"),
        r(JUNIOR_DTB_ID_2010, "U16", quarter, 20, "750"));
    var result = service.buildCurrentRankings(JUNIOR_DTB_ID_2010, rankings, quarter, null,
        List.of());
    assertEquals("U14", result.get(0).ageGroup());
    assertEquals("U16", result.get(1).ageGroup());
    assertEquals("U18", result.get(2).ageGroup());
  }

  // --- buildCompleteRankings ---

  @Test
  @DisplayName("buildCompleteRankings groups by date and maps quarter correctly")
  void buildCompleteRankingsGrouping() {
    var rankings = List.of(
        r(1, "m00", LocalDate.of(2024, Month.APRIL, 1), 5, "800"),
        r(1, "m00", LocalDate.of(2024, Month.JANUARY, 1), 6, "780"));
    var result = service.buildCompleteRankings(rankings);
    assertEquals(2, result.size());
    assertEquals("2024/Q2", result.get(0).date());
    assertEquals("2024/Q1", result.get(1).date());
  }

  @Test
  @DisplayName("buildCompleteRankings maps adult age groups to Aktive")
  void buildCompleteRankingsMapsAdultToAktive() {
    var rankings = List.of(r(1, "m00", LocalDate.of(2024, Month.APRIL, 1), 5, "800"));
    var result = service.buildCompleteRankings(rankings);
    assertEquals(1, result.size());
    assertTrue(result.getFirst().ageGroupPositions().containsKey("Aktive"));
  }

  @Test
  @DisplayName("buildCompleteRankings excludes ageGroupRanking and yobRanking rows")
  void buildCompleteRankingsFiltersFlags() {
    var date = LocalDate.of(2024, Month.APRIL, 1);
    var general = new Ranking(0L, 1, "Test", "Player", "GER", "U14", date, 5, "800", "TC Test",
        "HH", false, false, false);
    var ageGroup = new Ranking(0L, 1, "Test", "Player", "GER", "U14", date, 3, "800", "TC Test",
        "HH", true, false, false);
    var yob = new Ranking(0L, 1, "Test", "Player", "GER", "U14", date, 2, "800", "TC Test", "HH",
        false, true, false);
    var result = service.buildCompleteRankings(List.of(general, ageGroup, yob));
    assertEquals(1, result.size());
    assertEquals(5, result.getFirst().ageGroupPositions().get("U14"));
  }

  @Test
  @DisplayName("buildCompleteRankings skips rows with no QUARTER_MAP entry (e.g. month 2)")
  void buildCompleteRankingsSkipsUnknownMonth() {
    var rankings = List.of(r(1, "m00", LocalDate.of(2024, Month.FEBRUARY, 1), 5, "800"));
    var result = service.buildCompleteRankings(rankings);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("buildCompleteRankings excludes overall age group")
  void buildCompleteRankingsExcludesOverall() {
    var date = LocalDate.of(2024, Month.APRIL, 1);
    var result = service.buildCompleteRankings(List.of(
        r(1, "overall", date, 1, "850"),
        r(1, "m00", date, 5, "800")));
    assertEquals(1, result.size());
    assertFalse(result.getFirst().ageGroupPositions().containsKey("overall"));
    assertTrue(result.getFirst().ageGroupPositions().containsKey("Aktive"));
  }

  // --- buildAvailableData ---

  @Test
  @DisplayName("buildAvailableData silently skips rows with non-numeric year")
  void buildAvailableDataSkipsMalformedYear() {
    var rows = List.of(new CompleteRankingRow("abc/Q1", "800", Map.of()));
    var result = service.buildAvailableData(rows);
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("buildAvailableData silently skips rows with no slash separator")
  void buildAvailableDataSkipsNoSeparator() {
    var rows = List.of(new CompleteRankingRow("2024Q1", "800", Map.of()));
    var result = service.buildAvailableData(rows);
    assertTrue(result.isEmpty());
  }

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
    var prev = LocalDate.of(2024, Month.JANUARY, 1);
    var curr = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        r(ADULT_DTB_ID, "m00", curr, 5, "800"),
        r(ADULT_DTB_ID, "m00", prev, 5, "800"));
    var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, curr, prev, List.of());
    assertNull(result.getFirst().scoreChange());
  }

  static Stream<Arguments> scoreChangeFormatCases() {
    return Stream.of(
        Arguments.of("812",  "800",  "+12"),
        Arguments.of("66,9", "64,0", "+2,9"),
        Arguments.of("790",  "800",  "-10"),
        Arguments.of("N/A",  "800",  null)
    );
  }

  @ParameterizedTest(name = "curr={0}, prev={1} → {2}")
  @MethodSource("scoreChangeFormatCases")
  void scoreChangeFormats(String currScore, String prevScore, String expected) {
    var prev = LocalDate.of(2024, Month.JANUARY, 1);
    var curr = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        r(ADULT_DTB_ID, "m00", curr, 5, currScore),
        r(ADULT_DTB_ID, "m00", prev, 5, prevScore));
    var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, curr, prev, List.of());
    assertEquals(expected, result.getFirst().scoreChange());
  }

  @Test
  @DisplayName("score change treats non-numeric previous score as 0")
  void scoreChangeWithMalformedPreviousScore() {
    var prev = LocalDate.of(2024, Month.JANUARY, 1);
    var curr = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        r(ADULT_DTB_ID, "m00", curr, 5, "820"),
        r(ADULT_DTB_ID, "m00", prev, 5, "N/A"));
    var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, curr, prev, List.of());
    assertEquals("+820", result.getFirst().scoreChange());
  }

  @Test
  @DisplayName("score change is null when current score is a special value")
  void scoreChangeNullForSpecialScore() {
    var prev = LocalDate.of(2024, Month.JANUARY, 1);
    var curr = LocalDate.of(2024, Month.APRIL, 1);
    for (var special : List.of("PR", "Einst.", "0,0")) {
      var rankings = List.of(r(ADULT_DTB_ID, "m00", curr, 5, special),
          r(ADULT_DTB_ID, "m00", prev, 5, "800"));
      var result = service.buildCurrentRankings(ADULT_DTB_ID, rankings, curr, prev, List.of());
      assertNull(result.getFirst().scoreChange(), "expected null for special score: " + special);
    }
  }

  // --- buildDiagramData ---

  // Boy born 2010: marker=110, dtbId=11_000_001 → U14 in 2024 (double cohort U14)
  private static final int DTB_BOY_2010 = 11_000_001;

  private static Ranking rFlagged(int dtbId, String ageGroup, LocalDate date, int pos,
      boolean ageGroupRanking, boolean yobRanking) {
    return new Ranking(0L, dtbId, "Test", "Player", "GER", ageGroup, date, pos, "800", "TC Test",
        "HH",
        ageGroupRanking, yobRanking, false);
  }

  @Test
  @DisplayName("buildDiagramData returns only official AK ranking for youth player")
  void buildDiagramDataSelectsCorrectCohort() {
    var date = LocalDate.of(2024, Month.APRIL, 1);
    // Only the official AK ranking (ageGroupRanking=true) for U14 should be included;
    // G1-G5 rankings (ageGroupRanking=false) for any cohort are excluded from the diagram.
    var u14Official = rFlagged(DTB_BOY_2010, "U14", date, 8, true, false);
    var u12g1g5 = r(DTB_BOY_2010, "U12", date, 10, "800");
    var u14g1g5 = r(DTB_BOY_2010, "U14", date, 22, "800");
    var u16g1g5 = r(DTB_BOY_2010, "U16", date, 15, "800");
    var result = service.buildDiagramData(List.of(u14Official, u12g1g5, u14g1g5, u16g1g5));
    assertEquals(1, result.positions().size());
    assertEquals("U14", result.positions().getFirst().name());
    assertEquals(8, result.positions().getFirst().data().values().iterator().next());
  }

  @Test
  @DisplayName("buildDiagramData includes official AK ranking and excludes G1-G5 and yobRanking entries")
  void buildDiagramDataExcludesFlaggedEntries() {
    var date = LocalDate.of(2024, Month.APRIL, 1);
    var official = rFlagged(DTB_BOY_2010, "U14", date, 3, true, false);  // official AK → included
    var g1g5 = r(DTB_BOY_2010, "U14", date, 8, "800");                   // G1-G5 → excluded
    var yob = rFlagged(DTB_BOY_2010, "U14", date, 2, false, true);        // yobRanking → excluded
    var result = service.buildDiagramData(List.of(official, g1g5, yob));
    assertEquals(1, result.positions().size());
    assertEquals(3, result.positions().getFirst().data().values().iterator().next());
  }

  @Test
  @DisplayName("buildDiagramData creates staircase across quarters as player ages (official AK positions)")
  void buildDiagramDataStaircase() {
    var q1 = LocalDate.of(2023, Month.APRIL, 1);
    var q2 = LocalDate.of(2024, Month.APRIL, 1);
    var rankings = List.of(
        rFlagged(DTB_BOY_2010, "U14", q1, 12, true, false),  // official AK U14 at q1
        rFlagged(DTB_BOY_2010, "U14", q2, 8, true, false),   // official AK U14 at q2
        // G1-G5 U16 present in both quarters — excluded (ageGroupRanking=false)
        r(DTB_BOY_2010, "U16", q1, 30, "750"),
        r(DTB_BOY_2010, "U16", q2, 25, "800"));
    var result = service.buildDiagramData(rankings);
    assertEquals(1, result.positions().size());
    assertEquals("U14", result.positions().getFirst().name());
    assertEquals(2, result.positions().getFirst().data().size());
  }

  @Test
  @DisplayName("buildDiagramData maps adult age groups to Aktive")
  void buildDiagramDataAdult() {
    // Adult male player: dtbId in range 10_000_000-19_999_999 but ageGroup m00
    var date = LocalDate.of(2024, Month.APRIL, 1);
    var adult = r(10_000_001, "m00", date, 5, "820");
    var result = service.buildDiagramData(List.of(adult));
    assertEquals(1, result.positions().size());
    assertEquals("Aktive", result.positions().getFirst().name());
  }

  @Test
  @DisplayName("buildDiagramData returns empty diagram for empty input")
  void buildDiagramDataEmpty() {
    var result = service.buildDiagramData(List.of());
    assertTrue(result.positions().isEmpty());
  }

  // --- PlayerService helpers ---

  @Test
  @DisplayName("dtbIdRange from 7-digit prefix returns correct range")
  void dtbIdRangeSevenDigit() {
    var playerService = new PlayerService(null);
    var range = playerService.dtbIdRange("1234567");
    assertEquals(12345670, range[0]);
    assertEquals(12345679, range[1]);
  }

  @Test
  @DisplayName("dtbIdRange from 8-digit ID returns exact range")
  void dtbIdRangeEightDigit() {
    var playerService = new PlayerService(null);
    var range = playerService.dtbIdRange("12345678");
    assertEquals(12345678, range[0]);
    assertEquals(12345678, range[1]);
  }

  @Test
  @DisplayName("dtbIdRange handles non-numeric input gracefully")
  void dtbIdRangeInvalid() {
    var playerService = new PlayerService(null);
    var range = playerService.dtbIdRange("ABC");
    assertEquals(0, range[0]);
  }

  @Test
  @DisplayName("yobToMaleMarker converts last two yob digits to male marker")
  void yobToMaleMarker() {
    var playerService = new PlayerService(null);
    assertEquals(110, playerService.yobToMaleMarker("2010"));
    assertEquals(100, playerService.yobToMaleMarker("2000"));
    assertEquals(112, playerService.yobToMaleMarker("2012"));
  }
}
