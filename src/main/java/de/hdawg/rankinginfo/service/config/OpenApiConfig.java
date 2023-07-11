package de.hdawg.rankinginfo.service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

/**
 * definition for openAPI 3.0 docs.
 */
@OpenAPIDefinition(info = @Info(
    title = "ranking-info api",
    version = "1.0.0-SNAPSHOT",
    description = "backend api for the ranking-info.net application",
    license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
    contact = @Contact(url = "https://www.h-dawg.de", name = "Hauke Wesselmann", email = "ranking-info@h-dawg.de")
))
@Configuration
public class OpenApiConfig {

}
