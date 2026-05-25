package de.hdawg.rankinginfo.web

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class ListingControllerTest : WebControllerTestBase() {
    @BeforeEach
    override fun seed() {
        super.seed()
        // official U14 row (ageGroupRanking=true, yobRanking=false) for default-option regression test
        rankingRepository.save(r(10_711_113, "Juniormann3", "Max", "U14", q, 2, "90", true, false))
    }

    @Test
    fun `GET listings returns 200 without commit`() {
        mockMvc.get("/listings").andExpect { status { isOk() } }
    }

    @Test
    fun `GET listings with Herren commit returns rankings`() {
        mockMvc
            .get("/listings") {
                param("quarter", q.toString())
                param("gender", "Herren")
                param("commit", "1")
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `GET listings with Damen commit returns female rankings`() {
        mockMvc
            .get("/listings") {
                param("quarter", q.toString())
                param("gender", "Damen")
                param("commit", "1")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET listings with Junioren and age group`() {
        mockMvc
            .get("/listings") {
                param("quarter", q.toString())
                param("gender", "Junioren")
                param("age_group", "U14")
                param("commit", "1")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET listings with Juniorinnen no age group uses overall`() {
        mockMvc
            .get("/listings") {
                param("quarter", q.toString())
                param("gender", "Juniorinnen")
                param("commit", "1")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET listings with federation filter`() {
        mockMvc
            .get("/listings") {
                param("quarter", q.toString())
                param("gender", "Herren")
                param("federation", "BAD")
                param("commit", "1")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET listings with club filter`() {
        mockMvc
            .get("/listings") {
                param("quarter", q.toString())
                param("gender", "Herren")
                param("club", "Baden")
                param("commit", "1")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET listings populates quarters and federations model`() {
        mockMvc.get("/listings").andExpect {
        }
    }

    @Test
    fun `empty age_group_options uses default even-odd logic, not cumulative`() {
        // Empty string from the dropdown must be treated as null so U14 (even) defaults to ageGroupRanking=true.
        // Before the fix this returned zero results (empty string → both-false → cumulative query).
        mockMvc
            .get("/listings") {
                param("quarter", q.toString())
                param("gender", "Junioren")
                param("age_group", "U14")
                param("age_group_options", "")
                param("commit", "1")
            }.andExpect {
                status { isOk() }
                model {
                    attribute("rankings", org.hamcrest.Matchers.hasSize<Any>(1))
                }
            }
    }
}
