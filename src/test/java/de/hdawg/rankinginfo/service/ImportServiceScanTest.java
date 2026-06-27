package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImportServiceScanTest {

  @Autowired
  ImportService importService;

  @Autowired
  RankingRepository rankingRepository;

  @Autowired
  ImportHistoryRepository importHistoryRepository;

  @AfterEach
  void cleanup() {
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  // ── scanAndImport ────────────────────────────────────────────────────────

  @Test
  @DisplayName("scanAndImport imports CSV files from folder")
  void scanAndImportImportsCsvFilesFromFolder(@TempDir Path dir) throws IOException {
    copyFixture("Herren_20180401.csv", dir);
    importService.scanAndImport(dir.toString());
    assertEquals(10, rankingRepository.count());
  }

  @Test
  @DisplayName("scanAndImport skips already-imported files")
  void scanAndImportSkipsAlreadyImportedFiles(@TempDir Path dir) throws IOException {
    copyFixture("Herren_20180401.csv", dir);
    importService.scanAndImport(dir.toString());
    importService.scanAndImport(dir.toString());
    assertEquals(10, rankingRepository.count());
  }

  @Test
  @DisplayName("scanAndImport writes error log for invalid files")
  void scanAndImportWritesErrorLogForInvalidFiles(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("Herren_invalid.csv"), "bad,data");
    importService.scanAndImport(dir.toString());
    var errorLog = dir.resolve("error.log").toFile();
    assertTrue(errorLog.exists(), "error.log should be created for failed import");
  }

  @Test
  @DisplayName("scanAndImport handles non-existent folder gracefully")
  void scanAndImportHandlesNonExistentFolderGracefully() {
    importService.scanAndImport("/tmp/does_not_exist_xyz_123");
    assertEquals(0, rankingRepository.count());
  }

  @Test
  @DisplayName("scanAndImport appends separator between multiple errors")
  void scanAndImportAppendsSeparatorBetweenMultipleErrors(@TempDir Path dir) throws IOException {
    Files.writeString(dir.resolve("Herren_bad1.csv"), "bad");
    Files.writeString(dir.resolve("Herren_bad2.csv"), "bad");
    importService.scanAndImport(dir.toString());
    var content = Files.readString(dir.resolve("error.log"));
    assertTrue(content.contains("\n\n"), "Multiple errors should be separated by blank line");
  }

  // ── year-end ranking path (period.monthValue == 1) ───────────────────────

  @Test
  @DisplayName("Junioren import with January date creates year-end rankings")
  void juniorenImportWithJanuaryDateCreatesYearEndRankings(@TempDir Path dir) throws IOException {
    var src =
        getClass()
            .getClassLoader()
            .getResourceAsStream("fixtures/Junioren_20180401.csv")
            .readAllBytes();
    Files.write(dir.resolve("Junioren_20180101.csv"), src);
    importService.scanAndImport(dir.toString());
    var yearEnd = rankingRepository.findAll().stream().filter(r -> r.yearEndRanking()).toList();
    assertTrue(yearEnd.isEmpty() == false, "January import should produce year-end rankings");
  }

  // ── PDF import ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("scanAndImport imports Herren PDF and stores all entries")
  void scanAndImportImportsHerrenPdfAndStoresAllEntries(@TempDir Path dir) throws IOException {
    copyPdfFixture("herren.pdf", "Herren_20180401.pdf", dir);
    importService.scanAndImport(dir.toString());
    assertEquals(700, rankingRepository.count());
  }

  @Test
  @DisplayName("scanAndImport imports Damen PDF and stores all entries")
  void scanAndImportImportsDamenPdfAndStoresAllEntries(@TempDir Path dir) throws IOException {
    copyPdfFixture("damen.pdf", "Damen_20180401.pdf", dir);
    importService.scanAndImport(dir.toString());
    assertEquals(500, rankingRepository.count());
  }

  @Test
  @DisplayName("scanAndImport imports Junioren PDF and calculates derived rankings")
  void scanAndImportImportsJuniorenPdfAndCalculatesDerivedRankings(@TempDir Path dir)
      throws IOException {
    // Use a date matching the PDF's actual player cohorts (born 2008–2014),
    // so calculateRankings can resolve age-group brackets from the DTB IDs.
    copyPdfFixture("junioren.pdf", "Junioren_20260101.pdf", dir);
    importService.scanAndImport(dir.toString());
    long total = rankingRepository.count();
    assertTrue(total > 1659, "derived rankings must be added on top of 1659 raw entries");
    var derived =
        rankingRepository.findAll().stream()
            .filter(r -> r.yobRanking() || r.ageGroupRanking())
            .toList();
    assertFalse(derived.isEmpty(), "derived age-group and yob rankings must exist");
  }

  @Test
  @DisplayName("scanAndImport skips PDF when same quarter already imported as CSV")
  void scanAndImportSkipsPdfWhenCsvForSameQuarterAlreadyImported(@TempDir Path dir)
      throws IOException {
    copyFixture("Herren_20180401.csv", dir);
    importService.scanAndImport(dir.toString());
    long countAfterCsv = rankingRepository.count();

    copyPdfFixture("herren.pdf", "Herren_20180401.pdf", dir);
    importService.scanAndImport(dir.toString());
    assertEquals(countAfterCsv, rankingRepository.count());
    assertFalse(
        dir.resolve("error.log").toFile().exists(),
        "duplicate PDF import must not produce an error log entry");
  }

  @Test
  @DisplayName("scanAndImport skips CSV when same quarter already imported as PDF")
  void scanAndImportSkipsCsvWhenPdfForSameQuarterAlreadyImported(@TempDir Path dir)
      throws IOException {
    copyPdfFixture("herren.pdf", "Herren_20180401.pdf", dir);
    importService.scanAndImport(dir.toString());
    long countAfterPdf = rankingRepository.count();

    copyFixture("Herren_20180401.csv", dir);
    importService.scanAndImport(dir.toString());
    assertEquals(countAfterPdf, rankingRepository.count());
    assertFalse(
        dir.resolve("error.log").toFile().exists(),
        "duplicate CSV import must not produce an error log entry");
  }

  private void copyFixture(String name, Path dir) throws IOException {
    var bytes =
        getClass().getClassLoader().getResourceAsStream("fixtures/" + name).readAllBytes();
    Files.write(dir.resolve(name), bytes);
  }

  private void copyPdfFixture(String fixtureName, String targetName, Path dir) throws IOException {
    var bytes =
        getClass()
            .getClassLoader()
            .getResourceAsStream("fixtures/pdf/" + fixtureName)
            .readAllBytes();
    Files.write(dir.resolve(targetName), bytes);
  }
}
