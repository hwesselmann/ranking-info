package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankingServiceSlugTest {

  @Test
  @DisplayName("Herren maps to m00")
  void herrenToM00() {
    assertEquals("m00", RankingService.toAgeGroupSlug("Herren", null));
  }

  @Test
  @DisplayName("Damen maps to w00")
  void damenToW00() {
    assertEquals("w00", RankingService.toAgeGroupSlug("Damen", null));
  }

  @Test
  @DisplayName("Junioren with age group maps to m-prefix slug")
  void juniorenWithAgeGroup() {
    assertEquals("mu14", RankingService.toAgeGroupSlug("Junioren", "U14"));
  }

  @Test
  @DisplayName("Junioren without age group maps to overall")
  void juniorenWithoutAgeGroup() {
    assertEquals("overall", RankingService.toAgeGroupSlug("Junioren", null));
    assertEquals("overall", RankingService.toAgeGroupSlug("Junioren", ""));
  }

  @Test
  @DisplayName("Juniorinnen with age group maps to w-prefix slug")
  void juniorinnenWithAgeGroup() {
    assertEquals("wu12", RankingService.toAgeGroupSlug("Juniorinnen", "U12"));
  }

  @Test
  @DisplayName("Juniorinnen without age group maps to overall")
  void juniorinnenWithoutAgeGroup() {
    assertEquals("overall", RankingService.toAgeGroupSlug("Juniorinnen", null));
    assertEquals("overall", RankingService.toAgeGroupSlug("Juniorinnen", ""));
  }

  @Test
  @DisplayName("unknown gender maps to overall")
  void unknownGenderToOverall() {
    assertEquals("overall", RankingService.toAgeGroupSlug("Unknown", null));
  }
}
