package de.hdawg.rankinginfo.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class BearerTokenFilterTest {
    private val objectMapper = ObjectMapper()
    private val noOpChain = FilterChain { _, _ -> }

    @BeforeEach
    fun setUp() {
        System.setProperty("API_BEARER_TOKEN", "")
    }

    @AfterEach
    fun tearDown() {
        System.clearProperty("API_BEARER_TOKEN")
    }

    private fun filter(tokens: List<String> = listOf("valid-token")) = BearerTokenFilter(tokens, objectMapper)

    @Test
    fun `passes request with valid token`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer valid-token") }
        val response = MockHttpServletResponse()
        var chainCalled = false

        filter().doFilter(request, response, FilterChain { _, _ -> chainCalled = true })

        assert(chainCalled)
        assertEquals(200, response.status)
    }

    @Test
    fun `returns 401 without authorization header`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        filter().doFilter(request, response, noOpChain)

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `returns 401 with wrong token`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer wrong-token") }
        val response = MockHttpServletResponse()

        filter().doFilter(request, response, noOpChain)

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `returns 401 when header is not Bearer scheme`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Token valid-token") }
        val response = MockHttpServletResponse()

        filter().doFilter(request, response, noOpChain)

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status)
    }

    @Test
    fun `accepts token from configured tokens list`() {
        val filterWithToken = BearerTokenFilter(listOf("env-token"), objectMapper)
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer env-token") }
        val response = MockHttpServletResponse()
        var chainCalled = false

        filterWithToken.doFilter(request, response, FilterChain { _, _ -> chainCalled = true })

        assert(chainCalled)
    }

    @Test
    fun `response body contains error json on 401`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        filter().doFilter(request, response, noOpChain)

        val body = objectMapper.readTree(response.contentAsString)
        assertEquals("Unauthorized", body["error"].asText())
    }
}
