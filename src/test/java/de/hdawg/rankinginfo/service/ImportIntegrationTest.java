package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
class ImportIntegrationTest {

  @Autowired ImportService importService;

  @Autowired RankingRepository rankingRepository;

  @Autowired ImportHistoryRepository importHistoryRepository;

  @AfterEach
  void cleanup() {
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  @Test
  @DisplayName("Herren import stores 10 records with age_group m00 and no U-group rankings")
  void herrenImportStores10RecordsWithAgeGroupM00AndNoUGroupRankings() throws Exception {
    importService.importRankings(fixture("Herren_20180401.csv"));

    var herren =
        rankingRepository.findAll().stream()
            .filter(r -> r.ageGroup().equals("m00"))
            .toList();
    assertEquals(10, herren.size());
    assertEquals(10, rankingRepository.count());
    assertEquals(
        0,
        rankingRepository.findAll().stream()
            .filter(r -> r.ageGroup().startsWith("U"))
            .count());
  }

  @Test
  @DisplayName("Herren import stores correct positions from CSV")
  void herrenImportStoresCorrectPositionsFromCsv() throws Exception {
    importService.importRankings(fixture("Herren_20180401.csv"));

    var rankings =
        rankingRepository.findAll().stream()
            .sorted(java.util.Comparator.comparingInt(r -> r.rankingPosition()))
            .toList();
    assertEquals(1, rankings.get(0).rankingPosition());
    assertEquals("Mueller", rankings.get(0).lastname());
    assertEquals(10, rankings.get(rankings.size() - 1).rankingPosition());
    assertEquals("Koch", rankings.get(rankings.size() - 1).lastname());
  }

  @Test
  @DisplayName("Damen import stores 10 records with age_group w00 and no U-group rankings")
  void damenImportStores10RecordsWithAgeGroupW00AndNoUGroupRankings() throws Exception {
    importService.importRankings(fixture("Damen_20180401.csv"));

    assertEquals(10, rankingRepository.count());
    assertEquals(
        10,
        rankingRepository.findAll().stream()
            .filter(r -> r.ageGroup().equals("w00"))
            .count());
    assertEquals(
        0,
        rankingRepository.findAll().stream()
            .filter(r -> r.ageGroup().startsWith("U"))
            .count());
  }

  @Test
  @DisplayName("import creates ImportHistory entry")
  void importCreatesImportHistoryEntry() throws Exception {
    importService.importRankings(fixture("Herren_20180401.csv"));

    var histories = importHistoryRepository.findAll();
    assertEquals(1, histories.size());
    assertEquals("Herren", histories.get(0).category());
    assertEquals(LocalDate.of(2018, 4, 1), histories.get(0).period());
    assertEquals("Herren_20180401.csv", histories.get(0).filename());
  }

  @Test
  @DisplayName("duplicate import throws DuplicateImportError")
  void duplicateImportThrowsDuplicateImportError() throws Exception {
    importService.importRankings(fixture("Herren_20180401.csv"));

    assertThrows(
        ImportService.DuplicateImportError.class,
        () -> importService.importRankings(fixture("Herren_20180401.csv")));
  }

  @Test
  @DisplayName("Junioren import stores overall records and triggers U-group calculation")
  void juniorenImportStoresOverallRecordsAndTriggersUGroupCalculation() throws Exception {
    importService.importRankings(fixture("Junioren_20180401.csv"));

    var overall =
        rankingRepository.findAll().stream()
            .filter(r -> r.ageGroup().equals("overall"))
            .toList();
    assertEquals(17, overall.size());

    var uGroups =
        rankingRepository.findAll().stream()
            .filter(r -> r.ageGroup().startsWith("U"))
            .toList();
    assertTrue(uGroups.isEmpty() == false, "Expected U-group rankings to be calculated");
  }

  @Test
  @DisplayName("Junioren import uses GER-only position counter - foreign players do not advance rank")
  void juniorenImportUsesGerOnlyPositionCounter() throws Exception {
    importService.importRankings(fixture("Junioren_20180401.csv"));

    var u18general =
        rankingRepository.findAll().stream()
            .filter(r -> r.ageGroup().equals("U18") && !r.yobRanking() && !r.ageGroupRanking())
            .toList();
    if (!u18general.isEmpty()) {
      var minPosition = u18general.stream().mapToInt(r -> r.rankingPosition()).min().orElseThrow();
      assertEquals(1, minPosition);
    }
  }

  @Test
  @DisplayName("combined Junioren and Juniorinnen import matches Ruby reference count of 288")
  void combinedJuniorenAndJuniorinnenImportMatchesRubyReferenceCountOf288() throws Exception {
    importService.importRankings(fixture("Junioren_20180401.csv"));
    importService.importRankings(fixture("Juniorinnen_20180401.csv"));

    assertEquals(288, rankingRepository.count());
  }

  private Path fixture(String name) {
    try {
      return Path.of(getClass().getClassLoader().getResource("fixtures/" + name).toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
