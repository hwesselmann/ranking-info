package de.hdawg.rankinginfo.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures the SpringDoc OpenAPI bean with API metadata and the Bearer token security scheme.
 *
 * The security scheme named `bearerAuth` is referenced by the API controllers via
 * `@SecurityRequirement(name = "bearerAuth")`.
 */
@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(Info().title("ranking-info API").version("v1").description("German Tennis Youth & Adult Rankings API"))
            .components(
                Components().addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Token"),
                ),
            )
}
