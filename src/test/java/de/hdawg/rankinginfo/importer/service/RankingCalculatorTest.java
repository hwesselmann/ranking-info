package de.hdawg.rankinginfo.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.hdawg.rankinginfo.model.Federation;
import de.hdawg.rankinginfo.model.Nationality;
import de.hdawg.rankinginfo.model.Ranking;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankingCalculatorTest {

  private final RankingCalculator sut = new RankingCalculator();

  @DisplayName("test the calculation of ranking positions for a given list of players")
  @Test
  void testRankingCalculation() {
    LocalDate rankingPeriod = LocalDate.of(2022, 10, 1);
    List<Ranking> inRankings = new ArrayList<>();
    inRankings.add(createTestRankings(rankingPeriod, "10800001", Nationality.BEL, "1189,4", 60));
    inRankings.add(createTestRankings(rankingPeriod, "10800002", Nationality.GER, "1189,4", 60));
    inRankings.add(createTestRankings(rankingPeriod, "10800003", Nationality.GER, "1189,4", 110));
    inRankings.add(createTestRankings(rankingPeriod, "10800004", Nationality.GER, "Einst.", 110));
    inRankings.add(createTestRankings(rankingPeriod, "10800005", Nationality.JPN, "1189,4", 125));
    inRankings.add(createTestRankings(rankingPeriod, "10800006", Nationality.CRO, "1189,4", 125));
    inRankings.add(createTestRankings(rankingPeriod, "10800007", Nationality.GER, "1189,4", 126));
    inRankings.add(createTestRankings(rankingPeriod, "10800008", Nationality.GER, "PR", 126));
    inRankings.add(createTestRankings(rankingPeriod, "10800009", Nationality.GER, "1189,4", 400));
    inRankings.add(createTestRankings(rankingPeriod, "10800010", Nationality.GER, "1189,4", 401));

    List<Ranking> result =
        sut.performRankingCalculation(inRankings, rankingPeriod, "U14", false, false, false);

    assertEquals(10, result.size());
    assertEquals(1, result.get(0).getPosition());
    assertEquals(1, result.get(1).getPosition());
    assertEquals(2, result.get(2).getPosition());
    assertEquals(2, result.get(3).getPosition());
    assertEquals(3, result.get(4).getPosition());
    assertEquals(3, result.get(5).getPosition());
    assertEquals(3, result.get(6).getPosition());
    assertEquals(3, result.get(7).getPosition());
    assertEquals(4, result.get(8).getPosition());
    assertEquals(5, result.get(9).getPosition());
  }

  private Ranking createTestRankings(LocalDate rankingPeriod, String dtbId, Nationality nationality,
      String points, int position) {
    return new Ranking(rankingPeriod, dtbId, "Tennismann", "Max", points, nationality,
        Federation.WTV,
        "TC Musterstadt", "overall", position, false,
        false, false);
  }

  @DisplayName("check if a given player is in an age group given")
  @Test
  void checkForAgeGroup() {
    assertTrue(sut.isInAgeGroup("10800000", List.of(15, 16), LocalDate.of(2023, 7, 10)));
    assertTrue(sut.isInAgeGroup("10900000", List.of(13, 14), LocalDate.of(2023, 7, 10)));
    assertTrue(sut.isInAgeGroup("11000000", List.of(13, 14), LocalDate.of(2023, 7, 10)));
    assertTrue(sut.isInAgeGroup("10600000", List.of(15, 16, 17), LocalDate.of(2023, 7, 10)));
    assertFalse(sut.isInAgeGroup("10800000", List.of(11), LocalDate.of(2023, 7, 10)));
    assertFalse(sut.isInAgeGroup("11200000", List.of(12), LocalDate.of(2023, 7, 10)));
  }

  @DisplayName("check if a ranking position should be counted up according to the set criteria")
  @Test
  void checkIfARankingPositionsSHouldBeCountedUp() {
    assertTrue(sut.rankingCounterNeedsUpdate(Nationality.GER, "1,0"));
    assertFalse(sut.rankingCounterNeedsUpdate(Nationality.GER, "0,0"));
    assertFalse(sut.rankingCounterNeedsUpdate(Nationality.GER, "PR"));
    assertFalse(sut.rankingCounterNeedsUpdate(Nationality.GER, "Einst."));
    assertFalse(sut.rankingCounterNeedsUpdate(Nationality.ARG, "1,0"));
  }

  @DisplayName("check if list of eligible age groups is filled correctly")
  @Test
  void checkIfAgeGroupListGetsFilledCorrectly() {
    assertEquals(4, sut.getEligibleAgeGroups(14).size());
    assertEquals(1, sut.getEligibleAgeGroups(11).size());
    assertEquals(8, sut.getEligibleAgeGroups(18).size());
    assertEquals(13, sut.getEligibleAgeGroups(13).get(0));
    assertEquals(12, sut.getEligibleAgeGroups(13).get(1));
    assertEquals(11, sut.getEligibleAgeGroups(13).get(2));
  }

  @DisplayName("check if list of eligible age groups throws an exception if parameters are oob")
  @Test
  void checkIfAgeGroupListThrowsExceptionOnInvalidParameters() {
    assertThrows(IllegalStateException.class, () -> sut.getEligibleAgeGroups(10));
    assertThrows(IllegalStateException.class, () -> sut.getEligibleAgeGroups(19));
  }
}
