package de.hdawg.rankinginfo.web

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class ClubControllerTest : WebControllerTestBase() {
    @Test
    fun `GET clubs returns 200 without params`() {
        mockMvc.get("/clubs").andExpect { status { isOk() } }
    }

    @Test
    fun `GET clubs with commit and name returns club list`() {
        mockMvc
            .get("/clubs") {
                param("club", "Baden")
                param("commit", "1")
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `GET clubs search with no match returns empty clubs`() {
        mockMvc
            .get("/clubs") {
                param("club", "XYZNOTEXIST")
                param("commit", "1")
            }.andExpect { status { isOk() } }
    }

    @Test
    fun `GET club show returns 200`() {
        mockMvc.get("/clubs/TC Baden").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `GET club show for unknown club returns empty players`() {
        mockMvc.get("/clubs/UNKNOWN CLUB").andExpect {
            status { isOk() }
        }
    }
}
