package de.hdawg.rankinginfo.config

import com.fasterxml.jackson.databind.ObjectMapper
import de.hdawg.rankinginfo.api.security.BearerTokenFilter
import de.hdawg.rankinginfo.api.security.RateLimitFilter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/** Configuration properties for API token authentication, bound to the `api` prefix. */
@ConfigurationProperties(prefix = "api")
data class ApiProperties(
    val tokens: List<String> = emptyList(),
)

/**
 * Spring Security configuration defining two separate filter chains:
 * one for API endpoints (all `/api/v1/` routes) and one for all other web routes.
 *
 * The API chain applies [RateLimitFilter] followed by [BearerTokenFilter].
 * The web chain disables CSRF and permits all requests (auth handled at the application level).
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ApiProperties::class)
class SecurityConfig {
    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()

    @Bean
    @Order(1)
    fun apiSecurityFilterChain(
        http: HttpSecurity,
        apiProperties: ApiProperties,
        objectMapper: ObjectMapper,
    ): SecurityFilterChain {
        val rateLimitFilter = RateLimitFilter(objectMapper)
        val bearerFilter = BearerTokenFilter(apiProperties.tokens, objectMapper)

        http
            .securityMatcher("/api/v1/**")
            .csrf { it.disable() }
            .authorizeHttpRequests { auth -> auth.anyRequest().permitAll() }
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(bearerFilter, RateLimitFilter::class.java)

        return http.build()
    }

    @Bean
    @Order(2)
    fun webSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth -> auth.anyRequest().permitAll() }
        return http.build()
    }
}
