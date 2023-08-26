package de.hdawg.rankinginfo.importer;

import de.hdawg.rankinginfo.importer.repository.RankingImportRepository;
import de.hdawg.rankinginfo.importer.service.RankingCalculator;
import de.hdawg.rankinginfo.importer.service.RankingFileProcessor;
import de.hdawg.rankinginfo.model.Ranking;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * main entry class for triggering a ranking-file import.
 */
@Component
public class RankingImporter {


  private static final Logger log = LoggerFactory.getLogger(RankingImporter.class);
  private final RankingImportRepository repository;

  public RankingImporter(RankingImportRepository repository) {
    this.repository = repository;
  }

  /**
   * main method orchestrating the flow of a ranking file import process.
   */
  public void importRankings(String filename) {
    log.info("new ranking-file will be imported into the database");
    RankingFileProcessor rankingFileProcessor = new RankingFileProcessor();
    RankingCalculator rankingCalculator = new RankingCalculator();
    long importStarted = System.currentTimeMillis();
    List<Ranking> rankings = rankingFileProcessor.readRankingsFromImportFile(filename);
    if (!rankings.isEmpty()) {
      Map<String, Map<String, List<Ranking>>> calculatedRankings =
          rankingCalculator.calculateRankings(rankings);
      storeRankings(calculatedRankings);
    }
    long importFinished = System.currentTimeMillis();
    log.info("ranking import finished in {} miliseconds", (importFinished - importStarted));
  }

  private void storeRankings(Map<String, Map<String, List<Ranking>>> rankingsMap) {
    rankingsMap.forEach((key, rankingMap) -> {
      log.debug("storing entries of listing type {}", key);
      rankingMap
          .forEach((ageGroup, rankingList) -> repository.storeRankings(rankingList));
    });
  }
}
