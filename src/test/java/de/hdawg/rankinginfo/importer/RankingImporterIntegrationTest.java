package de.hdawg.rankinginfo.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.hdawg.rankinginfo.importer.repository.RankingImportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class RankingImporterIntegrationTest {

  @Autowired
  private RankingImporter sut;

  @Autowired
  private RankingImportRepository repository;

  @DisplayName("run the complete import dance")
  @Test
  void performRankingImport() {
    String filename =
        "src/test/resources/AlphaGesamtranglisteJuniorenfuerJugendturnierveranstalter_20221001.csv";
    sut.importRankings(filename);

    assertEquals(11360, repository.getEntryCount());
  }
}
