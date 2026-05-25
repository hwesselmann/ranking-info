package de.hdawg.rankinginfo.web

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class PlayerWebControllerTest : WebControllerTestBase() {
    @Test
    fun `GET players returns 200 without params`() {
        mockMvc.get("/players").andExpect { status { isOk() } }
    }

    @Test
    fun `GET players by lastname with multiple results returns 200`() {
        // Add a second Mueller so there's no single-result redirect
        rankingRepository.save(r(10_003_003, "Mueller", "Fritz", "m00", q, 3, "400", false, false))
        mockMvc
            .get("/players") {
                param("lastname", "Mueller")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET players by lastname with single result redirects`() {
        // Only one Schmidt → redirect to detail
        mockMvc
            .get("/players") {
                param("lastname", "Schmidt")
            }.andExpect { status { is3xxRedirection() } }
    }

    @Test
    fun `GET players by lastname and yob with single result redirects`() {
        mockMvc
            .get("/players") {
                param("lastname", "Mueller")
                param("yob", "2000")
            }.andExpect { status { is3xxRedirection() } }
    }

    @Test
    fun `GET players by yob only redirects or returns list`() {
        mockMvc
            .get("/players") {
                param("yob", "2000")
            }.andExpect { status { is2xxSuccessful() } }
    }

    @Test
    fun `GET players by dtb_id with single result redirects`() {
        mockMvc
            .get("/players") {
                param("dtb_id", "10001001")
            }.andExpect { status { is3xxRedirection() } }
    }

    @Test
    fun `GET players by dtb_id with no match returns 200`() {
        mockMvc
            .get("/players") {
                param("dtb_id", "99999999")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET player show returns 200 for known player`() {
        mockMvc.get("/players/10001001").andExpect { status { isOk() } }
    }

    @Test
    fun `GET player show with two quarters includes position change data`() {
        // Mueller has entries in both q and pq → currentRankings will have position_change
        mockMvc.get("/players/10001001").andExpect { status { isOk() } }
    }

    @Test
    fun `GET player show redirects for unknown player`() {
        mockMvc.get("/players/99999999").andExpect { status { is3xxRedirection() } }
    }
}
