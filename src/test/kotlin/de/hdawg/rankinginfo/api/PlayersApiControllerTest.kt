package de.hdawg.rankinginfo.api

import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.repository.ImportHistoryRepository
import de.hdawg.rankinginfo.repository.RankingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class PlayersApiControllerTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var rankingRepository: RankingRepository

    @Autowired lateinit var importHistoryRepository: ImportHistoryRepository

    private val auth = "Bearer test-api-token"
    private val q = LocalDate.of(2026, 4, 1)

    @BeforeEach
    fun seed() {
        val now = LocalDateTime.now()
        rankingRepository.saveAll(
            listOf(
                r(10_001_001, "Mustermann", "Max", "m00", q, 1, false, false),
                r(10_001_001, "Mustermann", "Max", "m00", LocalDate.of(2026, 1, 1), 3, false, false),
                r(10_002_002, "Schmidt", "Peter", "m00", q, 2, false, false),
            ),
        )
    }

    @AfterEach
    fun cleanup() {
        rankingRepository.deleteAll()
        importHistoryRepository.deleteAll()
    }

    // ── Auth ─────────────────────────────────────────────────────────────────

    @Test
    fun `search returns 401 without token`() {
        mockMvc.get("/api/v1/players?lastname=Mustermann").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `show returns 401 without token`() {
        mockMvc.get("/api/v1/players/10001001").andExpect { status { isUnauthorized() } }
    }

    // ── Search ───────────────────────────────────────────────────────────────

    @Test
    fun `search returns 400 when lastname missing`() {
        mockMvc
            .get("/api/v1/players") { header("Authorization", auth) }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error") { value("lastname parameter required") }
            }
    }

    @Test
    fun `search returns 404 when no player matches`() {
        mockMvc
            .get("/api/v1/players?lastname=NichtVorhanden") { header("Authorization", auth) }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `search returns matching players by lastname`() {
        mockMvc
            .get("/api/v1/players?lastname=Mustermann") { header("Authorization", auth) }
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].dtb_id") { value(10_001_001) }
                jsonPath("$.data[0].lastname") { value("Mustermann") }
            }
    }

    @Test
    fun `search is case-insensitive`() {
        mockMvc
            .get("/api/v1/players?lastname=mustermann") { header("Authorization", auth) }
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].lastname") { value("Mustermann") }
            }
    }

    @Test
    fun `search response contains request and data keys`() {
        mockMvc
            .get("/api/v1/players?lastname=Mustermann") { header("Authorization", auth) }
            .andExpect {
                jsonPath("$.request.lastname") { value("Mustermann") }
                jsonPath("$.request.total_count") { isNumber() }
                jsonPath("$.data") { isArray() }
            }
    }

    @Test
    fun `search data entry contains required fields`() {
        mockMvc
            .get("/api/v1/players?lastname=Mustermann") { header("Authorization", auth) }
            .andExpect {
                jsonPath("$.data[0].dtb_id") { isNumber() }
                jsonPath("$.data[0].lastname") { isString() }
                jsonPath("$.data[0].firstname") { isString() }
                jsonPath("$.data[0].club") { isString() }
                jsonPath("$.data[0].links.self") { isString() }
            }
    }

    @Test
    fun `search self link points to player detail`() {
        mockMvc
            .get("/api/v1/players?lastname=Mustermann") { header("Authorization", auth) }
            .andExpect {
                jsonPath("$.data[0].links.self") { value("/api/v1/players/10001001") }
            }
    }

    @Test
    fun `search filters by yob when provided`() {
        // dtb_id 10_001_001: male, yob prefix = "00" → yobMale = 100 → range 10000000..10099999
        mockMvc
            .get("/api/v1/players?lastname=Mustermann&yob=2000") { header("Authorization", auth) }
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].dtb_id") { value(10_001_001) }
            }
    }

    // ── Show ─────────────────────────────────────────────────────────────────

    @Test
    fun `show returns 404 for unknown dtb_id`() {
        mockMvc
            .get("/api/v1/players/99999999") { header("Authorization", auth) }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `show returns player detail for known dtb_id`() {
        mockMvc
            .get("/api/v1/players/10001001") { header("Authorization", auth) }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.dtb_id") { value(10_001_001) }
                jsonPath("$.data.lastname") { value("Mustermann") }
                jsonPath("$.data.firstname") { value("Max") }
            }
    }

    @Test
    fun `show returns required fields`() {
        mockMvc
            .get("/api/v1/players/10001001") { header("Authorization", auth) }
            .andExpect {
                jsonPath("$.data.dtb_id") { isNumber() }
                jsonPath("$.data.lastname") { isString() }
                jsonPath("$.data.firstname") { isString() }
                jsonPath("$.data.nationality") { isString() }
                jsonPath("$.data.club") { isString() }
                jsonPath("$.data.federation") { isString() }
                jsonPath("$.data.rankings") { isArray() }
            }
    }

    @Test
    fun `show rankings contain required fields`() {
        mockMvc
            .get("/api/v1/players/10001001") { header("Authorization", auth) }
            .andExpect {
                jsonPath("$.data.rankings[0].quarter") { isString() }
                jsonPath("$.data.rankings[0].age_group") { isString() }
                jsonPath("$.data.rankings[0].ranking_position") { isNumber() }
                jsonPath("$.data.rankings[0].score") { isString() }
            }
    }

    @Test
    fun `show returns multiple ranking entries across quarters`() {
        mockMvc
            .get("/api/v1/players/10001001") { header("Authorization", auth) }
            .andExpect {
                jsonPath("$.data.rankings.length()") { value(2) }
            }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun r(
        dtbId: Int,
        lastname: String,
        firstname: String,
        ageGroup: String,
        date: LocalDate,
        pos: Int,
        ageGroupRanking: Boolean,
        yobRanking: Boolean,
    ) = Ranking(
        0L,
        dtbId,
        lastname,
        firstname,
        "GER",
        ageGroup,
        date,
        pos,
        "100",
        "TC Test",
        "TST",
        ageGroupRanking,
        yobRanking,
        false,
        LocalDateTime.now(),
        LocalDateTime.now(),
    )
}
