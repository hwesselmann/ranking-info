package de.hdawg.rankinginfo.importer.service;

import de.hdawg.rankinginfo.model.Nationality;
import de.hdawg.rankinginfo.model.Ranking;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class for ranking position calculation.
 */
public class RankingCalculator {

  private static final Logger log = LoggerFactory.getLogger(RankingCalculator.class);

  private static final int BASE_YEAR_FOR_AGE_CALCULATION = 2000;
  private static final List<String> VALUES_FOR_PROTECTED_RANKINGS = List.of("0,0", "PR", "Einst.");

  /**
   * public interface for ranking calculation.
   *
   * @param importRankings list of overall youth rankings.
   * @return map of separate rankings per age group including special modifiers
   */
  public Map<String, Map<String, List<Ranking>>> calculateRankings(List<Ranking> importRankings) {
    LocalDate rankingPeriod = importRankings.get(0).getRankingPeriod();
    log.info("U11 will be yob {} in the current ranking period.", rankingPeriod.getYear() - 11);
    Collections.sort(importRankings);
    Map<String, Map<String, List<Ranking>>> calculatedRankings = new HashMap<>();
    calculatedRankings.put("official",
        calculateOfficialAgeGroupRankings(importRankings, rankingPeriod));
    calculatedRankings.put("overall",
        calculateOverallAgeGroupRankings(importRankings, rankingPeriod));
    calculatedRankings.put("yearOfBirth",
        calculateYearOfBirthRankings(importRankings, rankingPeriod));

    // check if this is the final ranking for this year. If so, do an extra calculation.
    if (rankingPeriod.getMonthValue() == 1) {
      calculatedRankings.put("endOfYear",
          calculateFinalRankingsForThisYear(importRankings, rankingPeriod));
    }

    return calculatedRankings;
  }

  /**
   * calculate the official rankings published by the German Tennis Federation (U12, U14, U16, U18).
   *
   * @param rankings      overall rankings to derive age group rankings from
   * @param rankingPeriod ranking period to calculate
   * @return map containing official listings
   */
  Map<String, List<Ranking>> calculateOfficialAgeGroupRankings(List<Ranking> rankings,
      LocalDate rankingPeriod) {
    Map<String, List<Ranking>> listings = new HashMap<>();
    List<Integer> ageGroups = List.of(12, 14, 16, 18);
    for (int ageGroup : ageGroups) {
      List<Ranking> filteredRankings = rankings.stream()
          .filter(r -> isInAgeGroup(r.getDtbId(), List.of(ageGroup, ageGroup - 1), rankingPeriod))
          .toList();

      String ageGroupString = "U" + ageGroup;
      listings.put(ageGroupString,
          performRankingCalculation(filteredRankings, rankingPeriod, ageGroupString,
              false, false, false));
    }
    return listings;
  }

  /**
   * calculate the rankings for all age groups while also taking younger players into account.
   *
   * @param rankings      overall rankings to derive age group rankings from
   * @param rankingPeriod ranking period to calculate
   * @return map containing calculated listings
   */
  Map<String, List<Ranking>> calculateOverallAgeGroupRankings(List<Ranking> rankings,
      LocalDate rankingPeriod) {
    Map<String, List<Ranking>> listings = new HashMap<>();
    List<Integer> ageGroups = List.of(11, 12, 13, 14, 15, 16, 17, 18);
    for (int ageGroup : ageGroups) {
      List<Integer> eligibleAgeGroups = getEligibleAgeGroups(ageGroup);
      List<Ranking> filteredRankings = rankings.stream()
          .filter(r -> isInAgeGroup(r.getDtbId(), eligibleAgeGroups, rankingPeriod))
          .toList();
      String ageGroupString = "U" + ageGroup;
      listings.put(ageGroupString,
          performRankingCalculation(filteredRankings, rankingPeriod, ageGroupString,
              true, false, false));
    }
    return listings;
  }

  List<Integer> getEligibleAgeGroups(int ageGroup) {
    if (ageGroup < 11 || ageGroup > 18) {
      throw new IllegalStateException();
    }
    List<Integer> eligibleAgeGroups = new ArrayList<>();
    for (int i = ageGroup; i >= 11; i--) {
      eligibleAgeGroups.add(i);
    }
    return eligibleAgeGroups;
  }

  /**
   * calculate the rankings for each year of birth separately.
   *
   * @param rankings      overall rankings to derive age group rankings from
   * @param rankingPeriod ranking period to calculate
   * @return map containing calculated listings
   */
  Map<String, List<Ranking>> calculateYearOfBirthRankings(List<Ranking> rankings,
      LocalDate rankingPeriod) {
    Map<String, List<Ranking>> listings = new HashMap<>();
    List<Integer> ageGroups = List.of(11, 12, 13, 14, 15, 16, 17, 18);
    for (int ageGroup : ageGroups) {
      List<Ranking> filteredRankings = rankings.stream()
          .filter(r -> isInAgeGroup(r.getDtbId(), List.of(ageGroup), rankingPeriod))
          .toList();
      String ageGroupString = "U" + ageGroup;
      listings.put(ageGroupString,
          performRankingCalculation(filteredRankings, rankingPeriod, ageGroupString,
              false, true, false));
    }
    return listings;
  }

  /**
   * calculate the final rankings for a year.
   *
   * @param rankings      overall rankings to derive age group rankings from
   * @param rankingPeriod ranking period to calculate
   * @return map containing calculated listings
   */
  Map<String, List<Ranking>> calculateFinalRankingsForThisYear(List<Ranking> rankings,
      LocalDate rankingPeriod) {
    Map<String, List<Ranking>> listings = new HashMap<>();
    List<Integer> ageGroups = List.of(12, 14, 16, 18);
    for (int ageGroup : ageGroups) {
      List<Ranking> filteredRankings = rankings.stream()
          .filter(r -> isInAgeGroup(r.getDtbId(), List.of(ageGroup, ageGroup - 1),
              rankingPeriod.minusDays(1L)))
          .toList();
      String ageGroupString = "U" + ageGroup;
      listings.put(ageGroupString,
          performRankingCalculation(filteredRankings, rankingPeriod, ageGroupString,
              false, false, true));
    }
    return listings;
  }

  List<Ranking> performRankingCalculation(List<Ranking> rankings, LocalDate rankingPeriod,
      String ageGroup,
      boolean isOverallRanking, boolean isYobRanking,
      boolean isEndOfYearRanking) {
    List<Ranking> calculatedRankings = new ArrayList<>();
    int lastRank = 0;
    int currentRank = 0;
    int rankingCounter = 1;

    for (Ranking currItem : rankings) {
      // check if a new ranking position should be applied
      if (currItem.getPosition() > lastRank) {
        lastRank = currItem.getPosition();
        currentRank = rankingCounter;
      }

      Ranking calculatedRanking =
          new Ranking(rankingPeriod, currItem.getDtbId(), currItem.getLastname(),
              currItem.getFirstname(), currItem.getPoints(), currItem.getNationality(),
              currItem.getFederation(),
              currItem.getClub(), ageGroup, currentRank, isYobRanking, isOverallRanking,
              isEndOfYearRanking);
      calculatedRankings.add(calculatedRanking);

      if (rankingCounterNeedsUpdate(currItem.getNationality(), currItem.getPoints())) {
        rankingCounter += 1;
      }
    }
    return calculatedRankings;
  }

  /**
   * check if a player is in a given list of age groups.
   * I am completely aware that someone needs to adapt the calculation in the year 2010
   * in case this app lives that long.
   *
   * @param dtbId         id of player to check
   * @param ageGroup      list of age groups to check against
   * @param rankingPeriod ranking period to verify against
   * @return is the given player part of the passed age groups?
   */
  boolean isInAgeGroup(String dtbId, List<Integer> ageGroup, LocalDate rankingPeriod) {
    int age = rankingPeriod.getYear() -
        (BASE_YEAR_FOR_AGE_CALCULATION + Integer.parseInt(dtbId.substring(1, 3)));
    return ageGroup.contains(age);
  }

  boolean rankingCounterNeedsUpdate(Nationality nationality, String points) {
    if (!nationality.equals(Nationality.GER)) {
      log.debug(
          "ranking position will not be counted up as this only applies to players with German nationality");
      return false;
    }
    if (VALUES_FOR_PROTECTED_RANKINGS.contains(points)) {
      log.debug("player has a protected ranking, so the ranking position will not be counted up");
      return false;
    }
    return true;
  }
}
