package de.hdawg.rankinginfo.api

import com.fasterxml.jackson.databind.ObjectMapper
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
class ListingsApiControllerTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var rankingRepository: RankingRepository

    @Autowired lateinit var importHistoryRepository: ImportHistoryRepository

    @Autowired lateinit var objectMapper: ObjectMapper

    private val quarter = "2026-04-01"
    private val prevQuarter = "2026-01-01"
    private val auth = "Bearer test-api-token"

    @BeforeEach
    fun seed() {
        val q = LocalDate.parse(quarter)
        val pq = LocalDate.parse(prevQuarter)
        val now = LocalDateTime.now()

        rankingRepository.saveAll(
            listOf(
                // current quarter – Herren
                ranking(10_001_001, "Mueller", "Hans", "m00", q, 1, "500", false, false, now),
                ranking(10_002_002, "Schmidt", "Peter", "m00", q, 2, "490", false, false, now),
                ranking(10_003_003, "Wagner", "Karl", "m00", q, 3, "480", false, false, now),
                // previous quarter – for position_change
                ranking(10_001_001, "Mueller", "Hans", "m00", pq, 3, "460", false, false, now),
                // Damen
                ranking(20_001_001, "Meyer", "Anna", "w00", q, 1, "500", false, false, now),
            ),
        )
    }

    @AfterEach
    fun cleanup() {
        rankingRepository.deleteAll()
        importHistoryRepository.deleteAll()
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    fun `returns 401 without authorization header`() {
        mockMvc.get("/api/v1/listings/$quarter/m00").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns 401 with wrong token`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", "Bearer wrong-token")
            }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns 200 with valid token`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect { status { isOk() } }
    }

    // ── Response structure ───────────────────────────────────────────────────

    @Test
    fun `response contains request and data keys`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                status { isOk() }
                jsonPath("$.request") { exists() }
                jsonPath("$.data") { exists() }
            }
    }

    @Test
    fun `request block contains expected meta fields`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.request.quarter") { value(quarter) }
                jsonPath("$.request.age_group") { value("m00") }
                jsonPath("$.request.total_count") { isNumber() }
                jsonPath("$.request.page") { value(1) }
                jsonPath("$.request.per_page") { value(25) }
            }
    }

    @Test
    fun `each data record contains required fields`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.data[0].dtb_id") { isNumber() }
                jsonPath("$.data[0].ranking_position") { isNumber() }
                jsonPath("$.data[0].lastname") { isString() }
                jsonPath("$.data[0].firstname") { isString() }
                jsonPath("$.data[0].score") { isString() }
                jsonPath("$.data[0].links.self") { isString() }
            }
    }

    @Test
    fun `data is ordered by ranking_position ascending`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.data[0].ranking_position") { value(1) }
                jsonPath("$.data[1].ranking_position") { value(2) }
                jsonPath("$.data[2].ranking_position") { value(3) }
            }
    }

    // ── Slug filtering ───────────────────────────────────────────────────────

    @Test
    fun `m00 slug returns only male dtb_ids`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.data[0].dtb_id") { value(10_001_001) }
            }
    }

    @Test
    fun `w00 slug returns only female dtb_ids`() {
        mockMvc
            .get("/api/v1/listings/$quarter/w00") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.data[0].dtb_id") { value(20_001_001) }
            }
    }

    // ── Pagination ───────────────────────────────────────────────────────────

    @Test
    fun `per_page limits result size`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00?per_page=1") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.data.length()") { value(1) }
                jsonPath("$.request.per_page") { value(1) }
            }
    }

    @Test
    fun `per_page is capped at 100`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00?per_page=9999") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.request.per_page") { value(100) }
            }
    }

    @Test
    fun `total_count reflects all records regardless of page size`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00?per_page=1") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.request.total_count") { value(3) }
            }
    }

    // ── Position change ──────────────────────────────────────────────────────

    @Test
    fun `position_change is calculated from previous quarter`() {
        // Mueller was rank 3 previously, now rank 1 → change = 3 - 1 = 2
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.data[0].dtb_id") { value(10_001_001) }
                jsonPath("$.data[0].position_change") { value(2) }
            }
    }

    @Test
    fun `position_change is null for new entries without previous ranking`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                jsonPath("$.data[1].dtb_id") { value(10_002_002) }
                jsonPath("$.data[1].position_change") { value(null as Any?) }
            }
    }

    // ── ETag ─────────────────────────────────────────────────────────────────

    @Test
    fun `response includes ETag header`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
            }.andExpect {
                header { exists("ETag") }
            }
    }

    @Test
    fun `returns 304 when ETag matches`() {
        val result =
            mockMvc
                .get("/api/v1/listings/$quarter/m00") {
                    header("Authorization", auth)
                }.andReturn()

        val etag = result.response.getHeader("ETag")!!

        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
                header("If-None-Match", etag)
            }.andExpect { status { isEqualTo(304) } }
    }

    @Test
    fun `returns 200 when ETag does not match`() {
        mockMvc
            .get("/api/v1/listings/$quarter/m00") {
                header("Authorization", auth)
                header("If-None-Match", "\"stale-etag\"")
            }.andExpect { status { isOk() } }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun ranking(
        dtbId: Int,
        lastname: String,
        firstname: String,
        ageGroup: String,
        date: LocalDate,
        pos: Int,
        score: String,
        ageGroupRanking: Boolean,
        yobRanking: Boolean,
        now: LocalDateTime,
    ) = Ranking(
        dtbId = dtbId,
        lastname = lastname,
        firstname = firstname,
        nationality = "GER",
        ageGroup = ageGroup,
        date = date,
        rankingPosition = pos,
        score = score,
        club = "TC Test",
        federation = "TST",
        ageGroupRanking = ageGroupRanking,
        yobRanking = yobRanking,
        yearEndRanking = false,
        createdAt = now,
        updatedAt = now,
    )
}
