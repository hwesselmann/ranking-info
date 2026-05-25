package de.hdawg.rankinginfo.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Servlet filter that enforces Bearer token authentication on API requests.
 *
 * Accepts tokens from two sources (checked in combination):
 * - `configuredTokens`: tokens provided via application configuration
 * - `API_BEARER_TOKEN` environment variable
 *
 * Returns HTTP 401 with a JSON error body when no valid token is present.
 */
class BearerTokenFilter(
    private val configuredTokens: List<String>,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    private val envToken: String? = System.getenv("API_BEARER_TOKEN")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
        if (token.isNullOrBlank() || !isValidToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            objectMapper.writeValue(response.writer, mapOf("error" to "Unauthorized"))
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun isValidToken(token: String): Boolean {
        val validTokens =
            buildSet {
                addAll(configuredTokens.filter { it.isNotBlank() })
                envToken?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
        return token in validTokens
    }
}
