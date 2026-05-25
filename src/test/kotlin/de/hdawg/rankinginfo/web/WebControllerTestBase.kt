package de.hdawg.rankinginfo.web

import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.repository.ImportHistoryRepository
import de.hdawg.rankinginfo.repository.RankingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
abstract class WebControllerTestBase {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var rankingRepository: RankingRepository

    @Autowired lateinit var importHistoryRepository: ImportHistoryRepository

    val q = LocalDate.of(2026, 4, 1)
    val pq = LocalDate.of(2026, 1, 1)

    @BeforeEach
    open fun seed() {
        val now = LocalDateTime.now()
        rankingRepository.saveAll(
            listOf(
                r(10_001_001, "Mueller", "Hans", "m00", q, 1, "500", false, false, "TC Baden", "BAD", now),
                r(10_002_002, "Schmidt", "Peter", "m00", q, 2, "490", false, false, "TC Other", "WTB", now),
                r(10_001_001, "Mueller", "Hans", "m00", pq, 3, "460", false, false, "TC Baden", "BAD", now),
                r(20_001_001, "Meyer", "Anna", "w00", q, 1, "500", false, false, "TC Baden", "BAD", now),
                r(10_711_111, "Juniormann", "Max", "overall", q, 1, "100", false, false, "TC Baden", "BAD", now),
                r(10_711_112, "Juniormann2", "Max", "U14", q, 1, "100", false, true, "TC Baden", "BAD", now),
            ),
        )
    }

    @AfterEach
    fun cleanup() {
        rankingRepository.deleteAll()
        importHistoryRepository.deleteAll()
    }

    fun r(
        dtbId: Int,
        lastname: String,
        firstname: String,
        ageGroup: String,
        date: LocalDate,
        pos: Int,
        score: String,
        ageGroupRanking: Boolean,
        yobRanking: Boolean,
        club: String = "TC Test",
        federation: String = "TST",
        now: LocalDateTime = LocalDateTime.now(),
    ) = Ranking(
        dtbId = dtbId,
        lastname = lastname,
        firstname = firstname,
        nationality = "GER",
        ageGroup = ageGroup,
        date = date,
        rankingPosition = pos,
        score = score,
        club = club,
        federation = federation,
        ageGroupRanking = ageGroupRanking,
        yobRanking = yobRanking,
        yearEndRanking = false,
        createdAt = now,
        updatedAt = now,
    )
}
