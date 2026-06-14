package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.hdawg.rankinginfo.domain.RankingCoding;

class AgeGroupResolverTest {

  // Boy born 2010: marker = (10 + 100) = 110, dtbId = 11_000_001
  private static final int DTB_BOY_2010 = 11_000_001;
  // Girl born 2008: marker = (8 + 200) = 208, dtbId = 20_800_001
  private static final int DTB_GIRL_2008 = 20_800_001;
  // Boy born 2006 (U18 in 2024): marker = (6 + 100) = 106, dtbId = 10_600_001
  private static final int DTB_BOY_2006 = 10_600_001;
  // Boy born 2013 (U11 in 2024): marker = (13 + 100) = 113, dtbId = 11_300_001
  private static final int DTB_BOY_2013 = 11_300_001;

  private static final LocalDate Q2_2024 = LocalDate.of(2024, 4, 1);

  // --- genderFactor ---

  @Test
  @DisplayName("genderFactor returns 100 for male DTB IDs")
  void genderFactorMale() {
    assertEquals(RankingCoding.GENDER_FACTOR_JUNIOREN, AgeGroupResolver.genderFactor(DTB_BOY_2010));
  }

  @Test
  @DisplayName("genderFactor returns 200 for female DTB IDs")
  void genderFactorFemale() {
    assertEquals(RankingCoding.GENDER_FACTOR_JUNIORINNEN, AgeGroupResolver.genderFactor(DTB_GIRL_2008));
  }

  // --- birthYear ---

  @Test
  @DisplayName("birthYear resolves correctly for boy born 2010 in Q2/2024")
  void birthYearBoy2010() {
    assertEquals(2010, AgeGroupResolver.birthYear(DTB_BOY_2010, 2024));
  }

  @Test
  @DisplayName("birthYear resolves correctly for girl born 2008 in Q2/2024")
  void birthYearGirl2008() {
    assertEquals(2008, AgeGroupResolver.birthYear(DTB_GIRL_2008, 2024));
  }

  @Test
  @DisplayName("birthYear uses century disambiguation when 2000+yy gives implausible age")
  void birthYearCenturyDisambiguation() {
    // yy=99 → candidate 2099, age = 2024 - 2099 = -75 (< 11), so falls back to 1999
    int dtbBoy1999 = (99 + 100) * RankingCoding.YOB_MULTIPLIER + 1; // marker=199
    assertEquals(1999, AgeGroupResolver.birthYear(dtbBoy1999, 2024));
  }

  // --- ageInQuarter ---

  @Test
  @DisplayName("ageInQuarter computes 14 for boy born 2010 in Q2/2024")
  void ageInQuarter2010() {
    assertEquals(14, AgeGroupResolver.ageInQuarter(DTB_BOY_2010, Q2_2024));
  }

  @Test
  @DisplayName("ageInQuarter computes 18 for boy born 2006 in Q2/2024")
  void ageInQuarter2006() {
    assertEquals(18, AgeGroupResolver.ageInQuarter(DTB_BOY_2006, Q2_2024));
  }

  @Test
  @DisplayName("ageInQuarter computes 11 for boy born 2013 in Q2/2024")
  void ageInQuarter2013() {
    assertEquals(11, AgeGroupResolver.ageInQuarter(DTB_BOY_2013, Q2_2024));
  }

  // --- currentSingleAgeGroup ---

  @Test
  @DisplayName("currentSingleAgeGroup returns U14 for boy born 2010 in 2024")
  void singleAgeGroupU14() {
    assertEquals("U14", AgeGroupResolver.currentSingleAgeGroup(DTB_BOY_2010, Q2_2024));
  }

  @Test
  @DisplayName("currentSingleAgeGroup returns U18 for boy born 2006 in 2024")
  void singleAgeGroupU18() {
    assertEquals("U18", AgeGroupResolver.currentSingleAgeGroup(DTB_BOY_2006, Q2_2024));
  }

  @Test
  @DisplayName("currentSingleAgeGroup returns U11 for boy born 2013 in 2024")
  void singleAgeGroupU11() {
    assertEquals("U11", AgeGroupResolver.currentSingleAgeGroup(DTB_BOY_2013, Q2_2024));
  }

  // --- currentDoubleAgeGroup ---

  @Test
  @DisplayName("currentDoubleAgeGroup: age 11 maps to U12")
  void doubleAgeGroupAge11() {
    assertEquals(12, AgeGroupResolver.currentDoubleAgeGroup(DTB_BOY_2013, Q2_2024).getAsInt());
  }

  @Test
  @DisplayName("currentDoubleAgeGroup: age 14 maps to U14")
  void doubleAgeGroupAge14() {
    assertEquals(14, AgeGroupResolver.currentDoubleAgeGroup(DTB_BOY_2010, Q2_2024).getAsInt());
  }

  @Test
  @DisplayName("currentDoubleAgeGroup: age 18 maps to U18")
  void doubleAgeGroupAge18() {
    assertEquals(18, AgeGroupResolver.currentDoubleAgeGroup(DTB_BOY_2006, Q2_2024).getAsInt());
  }

  @Test
  @DisplayName("currentDoubleAgeGroup: age 13 maps to U14")
  void doubleAgeGroupAge13() {
    // Boy born 2011: marker=111, dtbId=11_100_001 → age 13 in 2024
    int dtbBoy2011 = 11_100_001;
    assertEquals(14, AgeGroupResolver.currentDoubleAgeGroup(dtbBoy2011, Q2_2024).getAsInt());
  }

  @Test
  @DisplayName("currentDoubleAgeGroup: age 15 maps to U16")
  void doubleAgeGroupAge15() {
    // Boy born 2009: marker=109, dtbId=10_900_001 → age 15 in 2024
    int dtbBoy2009 = 10_900_001;
    assertEquals(16, AgeGroupResolver.currentDoubleAgeGroup(dtbBoy2009, Q2_2024).getAsInt());
  }

  @Test
  @DisplayName("currentDoubleAgeGroup: age 17 maps to U18")
  void doubleAgeGroupAge17() {
    // Boy born 2007: marker=107, dtbId=10_700_001 → age 17 in 2024
    int dtbBoy2007 = 10_700_001;
    assertEquals(18, AgeGroupResolver.currentDoubleAgeGroup(dtbBoy2007, Q2_2024).getAsInt());
  }

  @Test
  @DisplayName("currentDoubleAgeGroup is empty for adults (born 1999, age 25 in 2024)")
  void doubleAgeGroupEmptyForAdult() {
    int dtbBoy1999 = (99 + 100) * RankingCoding.YOB_MULTIPLIER + 1;
    assertTrue(AgeGroupResolver.currentDoubleAgeGroup(dtbBoy1999, Q2_2024).isEmpty());
  }

  // --- isJunior ---

  @Test
  @DisplayName("isJunior returns true for U14 player")
  void isJuniorTrue() {
    assertTrue(AgeGroupResolver.isJunior(DTB_BOY_2010, Q2_2024));
  }

  @Test
  @DisplayName("isJunior returns false for adult (born 1999)")
  void isJuniorFalseAdult() {
    int dtbBoy1999 = (99 + 100) * RankingCoding.YOB_MULTIPLIER + 1;
    assertFalse(AgeGroupResolver.isJunior(dtbBoy1999, Q2_2024));
  }
}
