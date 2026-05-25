package de.hdawg.rankinginfo.web

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class FederationControllerTest : WebControllerTestBase() {
    @Test
    fun `GET federations returns 200`() {
        mockMvc.get("/federations").andExpect { status { isOk() } }
    }

    @Test
    fun `GET federations populates federations model`() {
        mockMvc.get("/federations").andExpect {
        }
    }

    @Test
    fun `GET federations with empty DB returns empty map`() {
        rankingRepository.deleteAll()
        mockMvc.get("/federations").andExpect {
            status { isOk() }
        }
    }
}
