package de.hdawg.rankinginfo.service;

import java.time.LocalDate;
import java.util.OptionalInt;

import de.hdawg.rankinginfo.domain.RankingCoding;

public final class AgeGroupResolver {

  private static final int MIN_JUNIOR_AGE = 11;
  private static final int MAX_JUNIOR_AGE = 18;
  private static final int MIN_DOUBLE_COHORT = 12;
  private static final int CENTURY_2000 = 2000;
  private static final int CENTURY_1900 = 1900;

  private AgeGroupResolver() {}

  public static int genderFactor(int dtbId) {
    return dtbId < RankingCoding.FEMALE_DTB_ID_START
        ? RankingCoding.GENDER_FACTOR_JUNIOREN
        : RankingCoding.GENDER_FACTOR_JUNIORINNEN;
  }

  public static int birthYear(int dtbId, int quarterYear) {
    int marker = dtbId / RankingCoding.YOB_MULTIPLIER;
    int yy = marker - genderFactor(dtbId);
    int candidate = CENTURY_2000 + yy;
    int age = quarterYear - candidate;
    if (age < MIN_JUNIOR_AGE || age > MAX_JUNIOR_AGE) {
      candidate = CENTURY_1900 + yy;
    }
    return candidate;
  }

  public static int ageInQuarter(int dtbId, LocalDate quarter) {
    return quarter.getYear() - birthYear(dtbId, quarter.getYear());
  }

  public static String currentSingleAgeGroup(int dtbId, LocalDate quarter) {
    int age = ageInQuarter(dtbId, quarter);
    return "U" + Math.max(MIN_JUNIOR_AGE, age);
  }

  /** Returns the double-cohort (12/14/16/18) for the given player in the given quarter.
   * Empty if the player is not a junior in that quarter. */
  public static OptionalInt currentDoubleAgeGroup(int dtbId, LocalDate quarter) {
    int age = ageInQuarter(dtbId, quarter);
    if (age < MIN_JUNIOR_AGE || age > MAX_JUNIOR_AGE) return OptionalInt.empty();
    int doubled = age % 2 == 0 ? age : age + 1;
    return OptionalInt.of(Math.max(MIN_DOUBLE_COHORT, doubled));
  }

  public static boolean isJunior(int dtbId, LocalDate quarter) {
    int age = ageInQuarter(dtbId, quarter);
    return age >= MIN_JUNIOR_AGE && age <= MAX_JUNIOR_AGE;
  }
}
