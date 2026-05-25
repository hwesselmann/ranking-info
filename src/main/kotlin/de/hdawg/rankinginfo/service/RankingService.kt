package de.hdawg.rankinginfo.service

import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.repository.ImportHistoryRepository
import de.hdawg.rankinginfo.repository.RankingQueryFilter
import de.hdawg.rankinginfo.repository.RankingRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Parameters for filtering a ranking list query.
 *
 * @property quarter ISO date string for the ranking period (e.g. "2024-01-01")
 * @property ageGroupSlug slug encoding gender and age group (e.g. "m12", "wu18", "m00")
 * @property ageGroupOptions optional variant selector: "only_yob" or "include_younger"
 * @property federation optional federation abbreviation filter
 * @property club optional club name filter (case-insensitive substring)
 * @property yearEnd when true, selects year-end rankings (relevant for January quarters only)
 */
data class RankingFilter(
    val quarter: String,
    val ageGroupSlug: String,
    val ageGroupOptions: String? = null,
    val federation: String? = null,
    val club: String? = null,
    val yearEnd: Boolean = false,
)

/**
 * Service for querying and filtering ranking data.
 *
 * All read operations are transactional (read-only). Frequently accessed lookups
 * (available quarters, federations) are cached via Spring's caching abstraction.
 */
@Service
@Transactional(readOnly = true)
class RankingService(
    private val rankingRepository: RankingRepository,
    private val importHistoryRepository: ImportHistoryRepository,
) {
    companion object {
        private val AGE_GROUP_OPTION_FILTERS =
            mapOf(
                "only_yob" to Pair(true, false),
                "include_younger" to Pair(false, false),
            )
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        private fun slugToAgeGroup(slug: String): String =
            when {
                slug == "m00" -> "m00"
                slug == "w00" -> "w00"
                else -> "U${slug.substring(2).uppercase()}"
            }

        private fun buildQueryFilter(filter: RankingFilter): RankingQueryFilter {
            val quarter = LocalDate.parse(filter.quarter)
            val slug = filter.ageGroupSlug
            val ageGroup = slugToAgeGroup(slug)
            val isMale = slug.startsWith("m")
            val isAdult = ageGroup == "m00" || ageGroup == "w00"

            val (dtbIdStart, dtbIdEnd) =
                if (isMale) Pair(10_000_000, 19_999_999) else Pair(20_000_000, 29_999_999)

            val (yobRanking, ageGroupRanking) =
                if (isAdult) {
                    Pair(false, false)
                } else {
                    when (val opts = filter.ageGroupOptions) {
                        null -> {
                            val n = ageGroup.removePrefix("U").toIntOrNull() ?: 0
                            if (n % 2 == 0) Pair(false, true) else Pair(true, false)
                        }
                        else -> AGE_GROUP_OPTION_FILTERS[opts] ?: Pair(false, false)
                    }
                }

            val isJanuary = quarter.monthValue == 1
            return RankingQueryFilter(
                date = quarter,
                ageGroup = ageGroup,
                dtbIdStart = dtbIdStart,
                dtbIdEnd = dtbIdEnd,
                yobRanking = yobRanking,
                ageGroupRanking = ageGroupRanking,
                yearEndRanking = filter.yearEnd && isJanuary,
                federation = filter.federation?.takeIf { it.isNotBlank() },
                club = filter.club?.takeIf { it.isNotBlank() },
            )
        }
    }

    /**
     * Returns all available ranking quarters grouped by year, sorted descending.
     *
     * Dates with month=12/day=31 (year-end marker rows) are skipped.
     * The result is cached under `available_quarters`.
     */
    @Cacheable("available_quarters")
    fun fetchAvailableQuarters(): Map<String, List<Pair<String, String>>> {
        val years = mutableMapOf<String, MutableList<Pair<String, String>>>()
        for (date in rankingRepository.findDistinctDatesDesc()) {
            if (date.monthValue == 12 && date.dayOfMonth == 31) continue
            val adjusted = date.minusDays(1)
            years
                .getOrPut(adjusted.year.toString()) { mutableListOf() }
                .add(adjusted.format(DATE_FORMATTER) to date.toString())
        }
        return years.entries.sortedByDescending { it.key.toInt() }.associate { it.key to it.value }
    }

    /** Returns all distinct federation names, sorted alphabetically. Cached under `federations`. */
    @Cacheable("federations")
    fun fetchFederations(): List<String> = rankingRepository.findDistinctFederations()

    /**
     * Returns a paginated page of rankings matching the given [filter].
     *
     * @param filter the ranking filter criteria
     * @param page 1-based page number
     * @param perPage number of results per page
     */
    fun findFilteredRankings(
        filter: RankingFilter,
        page: Int,
        perPage: Int,
    ): Page<Ranking> = rankingRepository.findFiltered(buildQueryFilter(filter), page, perPage)

    /**
     * Looks up the ranking positions from the quarter immediately preceding [filter]'s quarter
     * for the given set of DTB IDs.
     *
     * Returns an empty map when no previous quarter exists or [dtbIds] is empty.
     *
     * @param filter used to determine the current quarter and apply the same filter criteria
     * @param dtbIds DTB IDs to look up; typically the IDs on the current page
     * @return map of DTB ID to previous ranking position
     */
    fun findPreviousPositions(
        filter: RankingFilter,
        dtbIds: List<Int>,
    ): Map<Int, Int> {
        if (dtbIds.isEmpty()) return emptyMap()
        val prevDate =
            rankingRepository
                .findDistinctDatesDesc()
                .firstOrNull { it < LocalDate.parse(filter.quarter) } ?: return emptyMap()

        val prevFilter = buildQueryFilter(filter.copy(quarter = prevDate.toString())).copy(dtbIds = dtbIds)
        return rankingRepository.findFiltered(prevFilter).associate { it.dtbId to it.rankingPosition }
    }

    /** Returns the timestamp of the most recent completed import, or null if no imports exist. */
    fun maxImportedAt(): LocalDateTime? = importHistoryRepository.findMaxImportedAt()
}
