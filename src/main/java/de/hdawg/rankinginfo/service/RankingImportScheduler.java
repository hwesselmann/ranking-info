package de.hdawg.rankinginfo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RankingImportScheduler {

  private static final Logger log = LoggerFactory.getLogger(RankingImportScheduler.class);

  private final ImportService importService;

  @Value("${import.folder}")
  private String importFolder;

  public RankingImportScheduler(ImportService importService) {
    this.importService = importService;
  }

  @Scheduled(cron = "${import.schedule:0 0 12 * * ?}")
  public void runImport() {
    log.info("Starting scheduled ranking import from '{}'", importFolder);
    importService.scanAndImport(importFolder);
    log.info("Scheduled ranking import completed");
  }
}
