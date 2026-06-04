package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImportServiceTest {

  // ── extractPeriodFromFilename ────────────────────────────────────────────

  @Test
  @DisplayName("extracts date from Junioren filename")
  void extractsDateFromJuniorenFilename() {
    assertEquals(
        LocalDate.of(2018, 3, 31),
        ImportService.extractPeriodFromFilename("Junioren_20180331.csv"));
  }

  @Test
  @DisplayName("extracts date from Herren filename")
  void extractsDateFromHerrenFilename() {
    assertEquals(
        LocalDate.of(2018, 4, 1),
        ImportService.extractPeriodFromFilename("Herren_20180401.csv"));
  }

  @Test
  @DisplayName("extracts date from Damen filename")
  void extractsDateFromDamenFilename() {
    assertEquals(
        LocalDate.of(2018, 4, 1),
        ImportService.extractPeriodFromFilename("Damen_20180401.csv"));
  }

  @Test
  @DisplayName("extracts date from December filename")
  void extractsDateFromDecemberFilename() {
    assertEquals(
        LocalDate.of(2012, 12, 31),
        ImportService.extractPeriodFromFilename("Junioren_20121231.csv"));
  }

  @Test
  @DisplayName("throws for filename without underscore")
  void throwsForFilenameWithoutUnderscore() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ImportService.extractPeriodFromFilename("Junioren-20180331.csv"));
  }

  // ── fileCategoryFromFilename ─────────────────────────────────────────────

  @Test
  @DisplayName("maps all four filename prefixes to categories")
  void mapsAllFourFilenamePrefixesToCategories() {
    assertEquals("junioren", ImportService.fileCategoryFromFilename("Junioren_20180401.csv"));
    assertEquals("juniorinnen", ImportService.fileCategoryFromFilename("Juniorinnen_20180401.csv"));
    assertEquals("herren", ImportService.fileCategoryFromFilename("Herren_20180401.csv"));
    assertEquals("damen", ImportService.fileCategoryFromFilename("Damen_20180401.csv"));
  }

  @Test
  @DisplayName("throws for unknown filename prefix")
  void throwsForUnknownFilenamePrefix() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ImportService.fileCategoryFromFilename("Unknown_20180401.csv"));
  }

  // ── yobRangeToFetch (exact port of Ruby test cases) ──────────────────────
  //
  // Ruby test used date 2019-03-31.
  // Male   gender_factor = 100
  // Female gender_factor = 200

  private final LocalDate period = LocalDate.of(2019, 3, 31);

  @Test
  @DisplayName("U11 male from yob 2008 returns 1 entry")
  void u11MaleFromYob2008Returns1Entry() {
    var result = ImportService.yobRangeToFetch("2008", 11, period, 100);
    assertEquals(1, result.size());
    assertEquals(108, result.get(0));
  }

  @Test
  @DisplayName("U12 male from yob 2008 returns 2 entries")
  void u12MaleFromYob2008Returns2Entries() {
    var result = ImportService.yobRangeToFetch("2008", 12, period, 100);
    assertEquals(2, result.size());
    assertEquals(108, result.get(0));
    assertEquals(107, result.get(1));
  }

  @Test
  @DisplayName("U14 female from yob 2008 returns 4 entries")
  void u14FemaleFromYob2008Returns4Entries() {
    var result = ImportService.yobRangeToFetch("2008", 14, period, 200);
    assertEquals(4, result.size());
    assertEquals(208, result.get(0));
    assertEquals(206, result.get(2));
  }

  @Test
  @DisplayName("U16 female from yob 2008 returns 6 entries")
  void u16FemaleFromYob2008Returns6Entries() {
    var result = ImportService.yobRangeToFetch("2008", 16, period, 200);
    assertEquals(6, result.size());
    assertEquals(208, result.get(0));
    assertEquals(204, result.get(4));
  }

  @Test
  @DisplayName("yobRangeToFetch is empty when player is too young for age group")
  void yobRangeToFetchIsEmptyWhenPlayerIsTooYoungForAgeGroup() {
    var result = ImportService.yobRangeToFetch("2012", 6, period, 100);
    assertTrue(result.isEmpty());
  }
}
