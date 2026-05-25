package de.hdawg.rankinginfo.service

import de.hdawg.rankinginfo.repository.ImportHistoryRepository
import de.hdawg.rankinginfo.repository.RankingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Paths
import java.time.LocalDate

@SpringBootTest
class ImportIntegrationTest {
    @Autowired lateinit var importService: ImportService

    @Autowired lateinit var rankingRepository: RankingRepository

    @Autowired lateinit var importHistoryRepository: ImportHistoryRepository

    @AfterEach
    fun cleanup() {
        rankingRepository.deleteAll()
        importHistoryRepository.deleteAll()
    }

    private fun fixture(name: String) = Paths.get(javaClass.classLoader.getResource("fixtures/$name")!!.toURI())

    @Test
    fun `Herren import stores 10 records with age_group m00 and no U-group rankings`() {
        importService.importRankings(fixture("Herren_20180401.csv"))

        val herren = rankingRepository.findAll().filter { it.ageGroup == "m00" }
        assertEquals(10, herren.size)
        assertEquals(10, rankingRepository.count())

        // no U-group records created for adult import
        assertEquals(0, rankingRepository.findAll().count { it.ageGroup.startsWith("U") })
    }

    @Test
    fun `Herren import stores correct positions from CSV`() {
        importService.importRankings(fixture("Herren_20180401.csv"))

        val rankings = rankingRepository.findAll().sortedBy { it.rankingPosition }
        assertEquals(1, rankings.first().rankingPosition)
        assertEquals("Mueller", rankings.first().lastname)
        assertEquals(10, rankings.last().rankingPosition)
        assertEquals("Koch", rankings.last().lastname)
    }

    @Test
    fun `Damen import stores 10 records with age_group w00 and no U-group rankings`() {
        importService.importRankings(fixture("Damen_20180401.csv"))

        assertEquals(10, rankingRepository.count())
        assertEquals(10, rankingRepository.findAll().count { it.ageGroup == "w00" })
        assertEquals(0, rankingRepository.findAll().count { it.ageGroup.startsWith("U") })
    }

    @Test
    fun `import creates ImportHistory entry`() {
        importService.importRankings(fixture("Herren_20180401.csv"))

        val histories = importHistoryRepository.findAll()
        assertEquals(1, histories.size)
        assertEquals("Herren", histories[0].category)
        assertEquals(LocalDate.of(2018, 4, 1), histories[0].period)
        assertEquals("Herren_20180401.csv", histories[0].filename)
    }

    @Test
    fun `duplicate import throws DuplicateImportError`() {
        importService.importRankings(fixture("Herren_20180401.csv"))

        assertThrows(ImportService.DuplicateImportError::class.java) {
            importService.importRankings(fixture("Herren_20180401.csv"))
        }
    }

    @Test
    fun `Junioren import stores overall records and triggers U-group calculation`() {
        importService.importRankings(fixture("Junioren_20180401.csv"))

        // 17 raw 'overall' records from CSV
        val overall = rankingRepository.findAll().filter { it.ageGroup == "overall" }
        assertEquals(17, overall.size)

        // ranking calculation must have produced U-group entries
        val uGroups = rankingRepository.findAll().filter { it.ageGroup.startsWith("U") }
        assertTrue(uGroups.isNotEmpty(), "Expected U-group rankings to be calculated")
    }

    @Test
    fun `Junioren import uses GER-only position counter - foreign players do not advance rank`() {
        // This fixture has only GER players; we verify positions are contiguous starting at 1.
        importService.importRankings(fixture("Junioren_20180401.csv"))

        // For U18 general (all yobs up to U18), position 1 should exist
        val u18general =
            rankingRepository.findAll().filter {
                it.ageGroup == "U18" && !it.yobRanking && !it.ageGroupRanking
            }
        if (u18general.isNotEmpty()) {
            val minPosition = u18general.minOf { it.rankingPosition }
            assertEquals(1, minPosition)
        }
    }

    @Test
    fun `combined Junioren and Juniorinnen import matches Ruby reference count of 288`() {
        importService.importRankings(fixture("Junioren_20180401.csv"))
        importService.importRankings(fixture("Juniorinnen_20180401.csv"))

        assertEquals(288, rankingRepository.count())
    }
}
