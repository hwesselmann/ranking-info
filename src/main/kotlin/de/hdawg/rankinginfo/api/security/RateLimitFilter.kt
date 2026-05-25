package de.hdawg.rankinginfo.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Servlet filter that enforces a per-client rate limit on all `/api/v1/` endpoints.
 *
 * Each client (identified by Authorization header or remote IP) gets an independent
 * token bucket allowing 1000 requests per hour. Returns HTTP 429 with a JSON error
 * body when the limit is exceeded.
 */
class RateLimitFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = !request.requestURI.startsWith("/api/v1")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val key = request.getHeader("Authorization") ?: request.remoteAddr
        val bucket = buckets.computeIfAbsent(key) { createBucket() }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            objectMapper.writeValue(response.writer, mapOf("error" to "Too Many Requests"))
        }
    }

    private fun createBucket(): Bucket =
        Bucket
            .builder()
            .addLimit(
                Bandwidth
                    .builder()
                    .capacity(1000)
                    .refillGreedy(1000, Duration.ofHours(1))
                    .build(),
            ).build()
}
