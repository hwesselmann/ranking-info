package de.hdawg.rankinginfo.service

import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.repository.RankingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class PlayerServiceTest {
    @Autowired lateinit var playerService: PlayerService

    @Autowired lateinit var rankingRepository: RankingRepository

    // DTB-ID prefix: male yob 2000 → dtb prefix 100 → 10000000..10099999
    // DTB-ID prefix: female yob 2000 → dtb prefix 200 → 20000000..20099999
    private val maleId = 10_001_001 // male
    private val femaleId = 20_001_001 // female

    @BeforeEach
    fun seed() {
        val now = LocalDateTime.now()
        val q = LocalDate.of(2026, 4, 1)
        rankingRepository.saveAll(
            listOf(
                r(maleId, "Mueller", "Hans", "m00", q, 1, now),
                r(femaleId, "Meyer", "Anna", "w00", q, 1, now),
                r(maleId, "Mueller", "Hans", "overall", q, 2, now),
            ),
        )
    }

    @AfterEach
    fun cleanup() {
        rankingRepository.deleteAll()
    }

    @Test
    fun `loadPlayerProfile returns correct player`() {
        val player = playerService.loadPlayerProfile(maleId)
        assertEquals("Mueller", player.lastname)
        assertEquals(maleId, player.dtbId)
    }

    @Test
    fun `loadPlayerProfile deduplicates clubs`() {
        val player = playerService.loadPlayerProfile(maleId)
        assertEquals(1, player.clubs.size)
    }

    @Test
    fun `findPlayersByLastname returns matching players`() {
        val results = playerService.findPlayersByLastname("Mueller")
        assertEquals(1, results.size)
        assertEquals("Mueller", results[0].lastname)
    }

    @Test
    fun `findPlayersByLastname is case-insensitive`() {
        val results = playerService.findPlayersByLastname("mueller")
        assertEquals(1, results.size)
    }

    @Test
    fun `findPlayersByLastname deduplicates by dtb_id`() {
        // maleId has two rankings; should appear once
        val results = playerService.findPlayersByLastname("Mueller")
        assertEquals(1, results.size)
    }

    @Test
    fun `findPlayersByLastnameAndYob filters by year of birth`() {
        // maleId = 10_001_001 → yobMale prefix = 100 → range 10000000..10099999
        val results = playerService.findPlayersByLastnameAndYob("Mueller", 100, 200)
        assertEquals(1, results.size)
    }

    @Test
    fun `findPlayersByLastnameAndYob returns empty for wrong yob`() {
        val results = playerService.findPlayersByLastnameAndYob("Mueller", 999, 1000)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `findPlayersByYob returns players in yob range`() {
        val results = playerService.findPlayersByYob(100, 200)
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `findPlayersByDtbIdRange returns player for exact id`() {
        val results = playerService.findPlayersByDtbIdRange(maleId, maleId)
        assertEquals(1, results.size)
        assertEquals(maleId, results[0].dtbId)
    }

    @Test
    fun `findPlayersByDtbIdRange returns empty for out-of-range`() {
        val results = playerService.findPlayersByDtbIdRange(99_000_000, 99_999_999)
        assertTrue(results.isEmpty())
    }

    private fun r(
        dtbId: Int,
        lastname: String,
        firstname: String,
        ageGroup: String,
        date: LocalDate,
        pos: Int,
        now: LocalDateTime,
    ) = Ranking(
        dtbId = dtbId,
        lastname = lastname,
        firstname = firstname,
        nationality = "GER",
        ageGroup = ageGroup,
        date = date,
        rankingPosition = pos,
        score = "100",
        club = "TC Test",
        federation = "TST",
        ageGroupRanking = false,
        yobRanking = false,
        yearEndRanking = false,
        createdAt = now,
        updatedAt = now,
    )
}
