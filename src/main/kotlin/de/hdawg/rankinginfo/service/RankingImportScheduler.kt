package de.hdawg.rankinginfo.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Scheduled component that triggers a ranking CSV import on a configurable cron schedule.
 *
 * The import folder is configured via `import.folder` and the cron expression via
 * `import.schedule` (default: daily at 12:00).
 */
@Component
class RankingImportScheduler(
    private val importService: ImportService,
) {
    private val log = LoggerFactory.getLogger(RankingImportScheduler::class.java)

    @Value("\${import.folder}")
    private lateinit var importFolder: String

    /** Triggers [ImportService.scanAndImport] for the configured import folder. */
    @Scheduled(cron = "\${import.schedule:0 0 12 * * ?}")
    fun runImport() {
        log.info("Starting scheduled ranking import from '{}'", importFolder)
        importService.scanAndImport(importFolder)
        log.info("Scheduled ranking import completed")
    }
}
