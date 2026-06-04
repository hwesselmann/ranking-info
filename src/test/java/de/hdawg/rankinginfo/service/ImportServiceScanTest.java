package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
class ImportServiceScanTest {

  @Autowired ImportService importService;

  @Autowired RankingRepository rankingRepository;

  @Autowired ImportHistoryRepository importHistoryRepository;

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

  private void copyFixture(String name, Path dir) throws IOException {
    var bytes =
        getClass().getClassLoader().getResourceAsStream("fixtures/" + name).readAllBytes();
    Files.write(dir.resolve(name), bytes);
  }
}
