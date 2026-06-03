package de.hdawg.rankinginfo.service

import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.repository.RankingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class RankingServiceFilterTest {
    @Autowired lateinit var rankingService: RankingService

    @Autowired lateinit var rankingRepository: RankingRepository

    private val q = LocalDate.of(2026, 4, 1)
    private val pq = LocalDate.of(2026, 1, 1)

    @BeforeEach
    fun seed() {
        val now = LocalDateTime.now()
        rankingRepository.saveAll(
            listOf(
                // adult male
                r(10_001_001, "m00", q, 1, "500", false, false, "TC Baden", "BAD", now),
                r(10_002_002, "m00", q, 2, "490", false, false, "TC Other", "WTB", now),
                r(10_001_001, "m00", pq, 3, "460", false, false, "TC Baden", "BAD", now),
                // adult female
                r(20_001_001, "w00", q, 1, "500", false, false, "TC Baden", "BAD", now),
                // youth U14 even age group → default = ageGroupRanking=true
                r(10_711_111, "U14", q, 1, "100", true, false, "TC Baden", "BAD", now),
                // youth U13 odd age group → default = yobRanking=true
                r(10_711_112, "U13", q, 1, "90", false, true, "TC Baden", "BAD", now),
                // year-end ranking in January period
                r(
                    10_001_001,
                    "m00",
                    LocalDate.of(2026, 1, 1),
                    1,
                    "500",
                    false,
                    false,
                    "TC Baden",
                    "BAD",
                    now,
                    yearEndRanking = true,
                ),
            ),
        )
    }

    @AfterEach
    fun cleanup() {
        rankingRepository.deleteAll()
    }

    @Test
    fun `adult male filter returns only male dtb_ids`() {
        val page =
            rankingService.findFilteredRankings(RankingFilter(q.toString(), "m00", null, null, null, false), 1, 100)
        assertTrue(page.content.all { it.dtbId in 10_000_000..19_999_999 })
    }

    @Test
    fun `adult female filter returns only female dtb_ids`() {
        val page =
            rankingService.findFilteredRankings(RankingFilter(q.toString(), "w00", null, null, null, false), 1, 100)
        assertTrue(page.content.all { it.dtbId in 20_000_000..29_999_999 })
    }

    @Test
    fun `U14 default filter selects ageGroupRanking=true`() {
        val page =
            rankingService.findFilteredRankings(RankingFilter(q.toString(), "mu14", null, null, null, false), 1, 100)
        assertTrue(page.content.all { it.ageGroupRanking })
    }

    @Test
    fun `U13 odd default filter selects yobRanking=true`() {
        val page =
            rankingService.findFilteredRankings(RankingFilter(q.toString(), "mu13", null, null, null, false), 1, 100)
        assertTrue(page.content.all { it.yobRanking })
    }

    @Test
    fun `only_yob option selects yobRanking=true`() {
        val page =
            rankingService.findFilteredRankings(
                RankingFilter(q.toString(), "mu13", "only_yob", null, null, false),
                1,
                100,
            )
        assertTrue(page.content.all { it.yobRanking })
    }

    @Test
    fun `include_younger option selects yobRanking=false and ageGroupRanking=false`() {
        val page =
            rankingService.findFilteredRankings(
                RankingFilter(q.toString(), "m00", "include_younger", null, null, false),
                1,
                100,
            )
        assertTrue(page.content.all { !it.yobRanking && !it.ageGroupRanking })
    }

    @Test
    fun `federation filter limits results`() {
        val page =
            rankingService.findFilteredRankings(
                RankingFilter(q.toString(), "m00", null, "BAD", null, false),
                1,
                100,
            )
        assertTrue(page.content.all { it.federation == "BAD" })
    }

    @Test
    fun `club filter is case-insensitive`() {
        val page =
            rankingService.findFilteredRankings(
                RankingFilter(q.toString(), "m00", null, null, "tc baden", false),
                1,
                100,
            )
        assertTrue(page.content.isNotEmpty())
    }

    @Test
    fun `yearEnd=true on January quarter selects yearEndRanking=true`() {
        val page =
            rankingService.findFilteredRankings(
                RankingFilter(pq.toString(), "m00", null, null, null, true),
                1,
                100,
            )
        assertTrue(page.content.all { it.yearEndRanking })
    }

    @Test
    fun `yearEnd=true on non-January quarter selects yearEndRanking=false`() {
        val page =
            rankingService.findFilteredRankings(
                RankingFilter(q.toString(), "m00", null, null, null, true),
                1,
                100,
            )
        assertTrue(page.content.all { !it.yearEndRanking })
    }

    @Test
    fun `fetchAvailableQuarters excludes Dec 31 year-end dates`() {
        rankingRepository.save(r(10_001_001, "m00", LocalDate.of(2025, 12, 31), 1, "500", false, false))
        val quarters = rankingService.fetchAvailableQuarters()
        val allDates = quarters.values.flatten().map { it.second }
        assertFalse(allDates.contains("2025-12-31"))
    }

    @Test
    fun `findPreviousPositions returns empty map when no dtbIds`() {
        val result =
            rankingService.findPreviousPositions(
                RankingFilter(q.toString(), "m00", null, null, null, false),
                emptyList(),
            )
        assertTrue(result.isEmpty())
    }

    private fun r(
        dtbId: Int,
        ageGroup: String,
        date: LocalDate,
        pos: Int,
        score: String,
        ageGroupRanking: Boolean,
        yobRanking: Boolean,
        club: String = "TC Test",
        federation: String = "TST",
        now: LocalDateTime = LocalDateTime.now(),
        yearEndRanking: Boolean = false,
    ) = Ranking(
        0L,
        dtbId,
        "Test",
        "Test",
        "GER",
        ageGroup,
        date,
        pos,
        score,
        club,
        federation,
        ageGroupRanking,
        yobRanking,
        yearEndRanking,
        now,
        now,
    )
}
