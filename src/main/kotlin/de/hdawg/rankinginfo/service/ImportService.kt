package de.hdawg.rankinginfo.service

import de.hdawg.rankinginfo.domain.ImportHistory
import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.repository.ImportHistoryRepository
import de.hdawg.rankinginfo.repository.RankingRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service responsible for importing ranking CSV files into the database.
 *
 * Each import reads raw "overall" ranking rows from a CSV and then derives
 * age-group, year-of-birth, and year-end ranking rows via [calculateRankings].
 * Duplicate imports (same category + period) are rejected via [DuplicateImportError].
 * Successful imports evict the `available_quarters` and `federations` caches.
 */
@Service
class ImportService(
    private val rankingRepository: RankingRepository,
    private val importHistoryRepository: ImportHistoryRepository,
) {
    private val log = LoggerFactory.getLogger(ImportService::class.java)

    companion object {
        private val SPECIAL_SCORES = setOf("0,0", "PR", "Einst.")
        private val GENDER_FACTORS = mapOf("junioren" to 100, "juniorinnen" to 200)
        private val AGE_GROUP_MAP = mapOf("herren" to "m00", "damen" to "w00", "junioren" to "overall", "juniorinnen" to "overall")
        private val CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

        private const val CSV_COL_POSITION = 0
        private const val CSV_COL_LASTNAME = 1
        private const val CSV_COL_FIRSTNAME = 2
        private const val CSV_COL_NATIONALITY = 3
        private const val CSV_COL_CLUB = 5
        private const val CSV_COL_SCORE = 6

        fun extractPeriodFromFilename(filename: String): LocalDate {
            val parts = File(filename).nameWithoutExtension.split("_")
            require(parts.size >= 2) { "Could not retrieve period part from filename '$filename'" }
            return LocalDate.parse(parts.last(), DateTimeFormatter.ofPattern("yyyyMMdd"))
        }

        fun fileCategoryFromFilename(filename: String): String =
            when (File(filename).nameWithoutExtension.split("_").first()) {
                "Junioren" -> "junioren"
                "Juniorinnen" -> "juniorinnen"
                "Herren" -> "herren"
                "Damen" -> "damen"
                else -> throw IllegalArgumentException("Unknown file category in filename '$filename'")
            }

        /**
         * Port of Ruby's yob_range_to_fetch: computes DTB-ID prefixes for all relevant year-of-birth classes
         * up to the given age_group.
         */
        fun yobRangeToFetch(
            yob: String,
            ageGroup: Int,
            period: LocalDate,
            genderFactor: Int,
        ): List<Int> {
            val classes = mutableListOf<Int>()
            val yobGenderMarker = yob.substring(2, 4).toInt() + genderFactor
            var i = 0
            while (ageGroup >= period.year - yob.toInt() + i) {
                classes.add(yobGenderMarker - i)
                i++
            }
            return classes
        }
    }

    /** Thrown when a CSV file for a category/period combination has already been imported. */
    class DuplicateImportError(
        message: String,
    ) : Exception(message)

    /**
     * Imports rankings from a single CSV file into the database.
     *
     * The filename must follow the pattern `<Category>_<yyyyMMdd>.csv` (e.g. `Junioren_20240401.csv`).
     * Stores raw records, derives age-group/YOB/year-end rankings for junior categories,
     * and records the import in [ImportHistory].
     *
     * @param file path to the CSV file to import
     * @throws DuplicateImportError if the same category/period has already been imported
     * @throws IllegalArgumentException if the filename cannot be parsed
     */
    @Transactional
    @CacheEvict(cacheNames = ["available_quarters", "available_dates", "federations"], allEntries = true)
    fun importRankings(file: Path) {
        val filename = file.fileName.toString()
        val period = extractPeriodFromFilename(filename)
        val category = fileCategoryFromFilename(filename)
        val capitalizedCategory = category.replaceFirstChar { it.uppercase() }

        if (importHistoryRepository.existsByCategoryAndPeriod(capitalizedCategory, period)) {
            throw DuplicateImportError("Rankings for '$category' / $period have already been imported")
        }

        val ageGroup = AGE_GROUP_MAP[category] ?: error("No age group mapping for category '$category'")
        storeFromCsv(file, period, ageGroup)

        GENDER_FACTORS[category]?.let { genderFactor ->
            calculateRankings(period, genderFactor)
        }

        val now = LocalDateTime.now()
        importHistoryRepository.save(
            ImportHistory(0L, filename, capitalizedCategory, period, now, now, now),
        )
        log.info("Import of '{}' completed", filename)
    }

    private fun storeFromCsv(
        file: Path,
        period: LocalDate,
        ageGroup: String,
    ) {
        val now = LocalDateTime.now()
        val records = mutableListOf<Ranking>()
        file.toFile().bufferedReader(Charsets.UTF_8).useLines { lines ->
            for (line in lines) {
                val parts = line.split(CSV_SPLIT_REGEX)
                if (parts.size < 7) continue
                val dtbParts = parts[4].trim().split(" ")
                if (dtbParts.size < 2) continue
                val dtbId = dtbParts[0].toIntOrNull() ?: continue
                records +=
                    Ranking(
                        0L,
                        dtbId,
                        parts[CSV_COL_LASTNAME].trim(),
                        parts[CSV_COL_FIRSTNAME].trim(),
                        parts[CSV_COL_NATIONALITY].trim().ifEmpty { "nil" },
                        ageGroup,
                        period,
                        parts[CSV_COL_POSITION].trim().toIntOrNull() ?: 0,
                        parts[CSV_COL_SCORE].trim().removeSurrounding("\""),
                        parts[CSV_COL_CLUB].trim().removeSurrounding("\""),
                        dtbParts[1],
                        false,
                        false,
                        false,
                        now,
                        now,
                    )
            }
        }
        rankingRepository.saveAll(records)
        log.debug("Stored {} records from CSV", records.size)
    }

    private fun calculateRankings(
        period: LocalDate,
        genderFactor: Int,
    ) {
        val yobYoungest = (period.year - 11).toString()

        // general and yob rankings for U11–U18
        for (ageGroup in 11..18) {
            val yobsGeneral = yobRangeToFetch(yobYoungest, ageGroup, period, genderFactor).sorted()
            rankingsForAgeRange(yobsGeneral, period, ageGroup, yobRanking = false, ageGroupRanking = false, yearEndRanking = false)

            val yobSingle = listOf((period.year - ageGroup).toString().substring(2, 4).toInt() + genderFactor)
            rankingsForAgeRange(yobSingle, period, ageGroup, yobRanking = true, ageGroupRanking = false, yearEndRanking = false)
        }

        // official age group rankings U12/U14/U16/U18
        for (ageGroup in listOf(12, 14, 16, 18)) {
            val yobs =
                listOf(
                    (period.year - ageGroup).toString().substring(2, 4).toInt() + genderFactor,
                    (period.year - ageGroup).toString().substring(2, 4).toInt() + genderFactor + 1,
                ).sorted()
            rankingsForAgeRange(yobs, period, ageGroup, yobRanking = false, ageGroupRanking = true, yearEndRanking = false)
        }

        // year-end rankings only if period month == January
        if (period.monthValue == 1) {
            val yeDate = period.minusDays(1)
            for (ageGroup in listOf(12, 14, 16, 18)) {
                val yobs =
                    listOf(
                        (yeDate.year - ageGroup).toString().substring(2, 4).toInt() + genderFactor,
                        (yeDate.year - ageGroup).toString().substring(2, 4).toInt() + genderFactor + 1,
                    ).sorted()
                rankingsForAgeRange(yobs, period, ageGroup, yobRanking = false, ageGroupRanking = true, yearEndRanking = true)
            }
        }
    }

    private fun rankingsForAgeRange(
        classesToRetrieve: List<Int>,
        period: LocalDate,
        ageGroup: Int,
        yobRanking: Boolean,
        ageGroupRanking: Boolean,
        yearEndRanking: Boolean,
    ) {
        val idStart = classesToRetrieve.first() * 100_000
        val idEnd = (classesToRetrieve.last() * 100_000) + 99_999

        val rankingsFromDb = rankingRepository.findForAgeRangeInPeriod(period, idStart, idEnd)

        var lastRank = 0
        var startRanking = 0
        var countUp = 1
        val now = LocalDateTime.now()
        val records = mutableListOf<Ranking>()

        for (curr in rankingsFromDb) {
            if (curr.rankingPosition > lastRank) {
                lastRank = curr.rankingPosition
                startRanking = countUp
            }
            records +=
                Ranking(
                    0L,
                    curr.dtbId,
                    curr.lastname,
                    curr.firstname,
                    curr.nationality,
                    "U$ageGroup",
                    curr.date,
                    startRanking,
                    curr.score,
                    curr.club,
                    curr.federation,
                    ageGroupRanking,
                    yobRanking,
                    yearEndRanking,
                    now,
                    now,
                )
            // foreign players and players with special scores do not advance the counter
            if (curr.nationality == "GER" && curr.score !in SPECIAL_SCORES) {
                countUp++
            }
        }

        if (records.isNotEmpty()) {
            rankingRepository.saveAll(records)
        }
    }

    /**
     * Scans a directory for CSV files and imports each one, skipping already-imported files.
     *
     * Import errors are logged and optionally appended to an error log file.
     *
     * @param folderPath path to the directory containing CSV files
     * @param errorLogPath optional path for an error log file; defaults to `<folderPath>/error.log`
     */
    @CacheEvict(cacheNames = ["available_quarters", "available_dates", "federations"], allEntries = true)
    fun scanAndImport(
        folderPath: String,
        errorLogPath: String? = null,
    ) {
        val folder = File(folderPath)
        if (!folder.isDirectory) {
            log.warn("Import folder '{}' does not exist or is not a directory", folderPath)
            return
        }
        val csvFiles = folder.listFiles { f -> f.extension.equals("csv", ignoreCase = true) } ?: return
        for (csvFile in csvFiles) {
            try {
                importRankings(csvFile.toPath())
                log.info("Imported '{}'", csvFile.name)
            } catch (e: DuplicateImportError) {
                log.info("Skipping '{}' (already imported)", csvFile.name)
            } catch (e: Exception) {
                val logFile = errorLogPath ?: "$folderPath/error.log"
                writeErrorLog(logFile, "${csvFile.name}: ${e.message}")
                log.error("Error importing '{}': {}", csvFile.name, e.message)
            }
        }
    }

    private fun writeErrorLog(
        logFilePath: String,
        message: String,
    ) {
        val logFile = File(logFilePath)
        val separator = if (logFile.exists()) "\n\n" else ""
        logFile.appendText("$separator$message")
    }
}
