package de.hdawg.rankinginfo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.hdawg.rankinginfo.config.ImportProperties;

@SuppressWarnings("PMD.GuardLogStatement")
@Component
public class RankingImportScheduler {

  private static final Logger log = LoggerFactory.getLogger(RankingImportScheduler.class);

  private final ImportService importService;
  private final ImportProperties importProperties;

  public RankingImportScheduler(ImportService importService, ImportProperties importProperties) {
    this.importService = importService;
    this.importProperties = importProperties;
  }

  @Scheduled(cron = "${import.schedule:0 0 12 * * ?}")
  public void runImport() {
    log.info("Starting scheduled ranking import from '{}'", importProperties.folder());
    importService.scanAndImport(importProperties.folder());
    log.info("Scheduled ranking import completed");
  }
}
