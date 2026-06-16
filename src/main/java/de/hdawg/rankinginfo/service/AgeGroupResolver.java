package de.hdawg.rankinginfo.service;

import java.time.LocalDate;
import java.util.OptionalInt;

import de.hdawg.rankinginfo.domain.RankingCoding;

/// Derives a player's age and competition age groups from their DTB ID and a ranking quarter
/// date.
///
/// DTB IDs encode gender and, for youth players, the 2-digit birth year: dividing the ID by
/// [RankingCoding#YOB_MULTIPLIER] yields a marker holding a gender factor
/// ([RankingCoding#GENDER_FACTOR_JUNIOREN] or [RankingCoding#GENDER_FACTOR_JUNIORINNEN]) plus
/// that birth year. All methods are static; the class is not instantiable.
public final class AgeGroupResolver {

  private static final int MIN_JUNIOR_AGE = 11;
  private static final int MAX_JUNIOR_AGE = 18;
  private static final int MIN_DOUBLE_COHORT = 12;
  private static final int CENTURY_2000 = 2000;
  private static final int CENTURY_1900 = 1900;

  private AgeGroupResolver() {}

  /// Returns the gender factor encoded in `dtbId`, based on which DTB ID range it falls in.
  ///
  /// @param dtbId the player's DTB ID
  /// @return [RankingCoding#GENDER_FACTOR_JUNIOREN] for male IDs, or
  ///     [RankingCoding#GENDER_FACTOR_JUNIORINNEN] for female IDs
  public static int genderFactor(int dtbId) {
    return dtbId < RankingCoding.FEMALE_DTB_ID_START
        ? RankingCoding.GENDER_FACTOR_JUNIOREN
        : RankingCoding.GENDER_FACTOR_JUNIORINNEN;
  }

  /// Decodes the birth year encoded in `dtbId`.
  ///
  /// The DTB ID only encodes the last two digits of the birth year; this resolves the century
  /// by preferring `20xx`, falling back to `19xx` if that would place the player's age in
  /// `quarterYear` outside the junior age range (useful for adult players, whose IDs encode the
  /// same two digits but aren't meant to be read as a birth year at all).
  ///
  /// @param dtbId the player's DTB ID
  /// @param quarterYear the year of the ranking quarter to resolve the age against
  /// @return the decoded birth year
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

  /// Computes the player's age as of `quarter`'s year, from the birth year decoded by
  /// [#birthYear(int, int)].
  ///
  /// @param dtbId the player's DTB ID
  /// @param quarter the ranking quarter date
  /// @return the player's age during `quarter`
  public static int ageInQuarter(int dtbId, LocalDate quarter) {
    return quarter.getYear() - birthYear(dtbId, quarter.getYear());
  }

  /// Returns the player's current single-year age bracket, clamped to at least `U11`.
  ///
  /// @param dtbId the player's DTB ID
  /// @param quarter the ranking quarter date
  /// @return the single age-group label, e.g. `"U14"`
  public static String currentSingleAgeGroup(int dtbId, LocalDate quarter) {
    int age = ageInQuarter(dtbId, quarter);
    return "U" + Math.max(MIN_JUNIOR_AGE, age);
  }

  /// Returns the double-cohort age group (`U12`/`U14`/`U16`/`U18`) for the player in `quarter`.
  ///
  /// @param dtbId the player's DTB ID
  /// @param quarter the ranking quarter date
  /// @return the double-cohort age, e.g. `14`; empty if the player is not a junior in that
  ///     quarter
  public static OptionalInt currentDoubleAgeGroup(int dtbId, LocalDate quarter) {
    int age = ageInQuarter(dtbId, quarter);
    if (age < MIN_JUNIOR_AGE || age > MAX_JUNIOR_AGE) return OptionalInt.empty();
    int doubled = age % 2 == 0 ? age : age + 1;
    return OptionalInt.of(Math.max(MIN_DOUBLE_COHORT, doubled));
  }

  /// Returns whether the player falls within the junior age range in `quarter`.
  ///
  /// @param dtbId the player's DTB ID
  /// @param quarter the ranking quarter date
  /// @return `true` if the player's age in `quarter` is between 11 and 18 inclusive
  public static boolean isJunior(int dtbId, LocalDate quarter) {
    int age = ageInQuarter(dtbId, quarter);
    return age >= MIN_JUNIOR_AGE && age <= MAX_JUNIOR_AGE;
  }
}
