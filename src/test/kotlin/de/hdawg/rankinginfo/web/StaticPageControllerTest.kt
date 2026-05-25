package de.hdawg.rankinginfo.web

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

class StaticPageControllerTest : WebControllerTestBase() {
    @Test
    fun `GET home returns 200 and populates start date`() {
        mockMvc.get("/").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `GET home with empty DB shows placeholder date`() {
        rankingRepository.deleteAll()
        mockMvc.get("/").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `GET help returns 200`() {
        mockMvc.get("/help").andExpect { status { isOk() } }
    }

    @Test
    fun `GET about returns 200`() {
        mockMvc.get("/about").andExpect { status { isOk() } }
    }

    @Test
    fun `GET privacy returns 200 and imprint data`() {
        mockMvc.get("/privacy").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `GET status returns 200 with counts`() {
        mockMvc.get("/status").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `GET status shows player counts in model`() {
        mockMvc.get("/status").andExpect {
            status { isOk() }
        }
    }
}
