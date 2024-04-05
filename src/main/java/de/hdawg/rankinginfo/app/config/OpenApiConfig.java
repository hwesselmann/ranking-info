package de.hdawg.rankinginfo.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
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
),
    servers = {
        @Server(url = "http://127.0.0.1:8080", description = "local development system"),
        @Server(url = "https://ranking-info.net/api", description = "production system")
    },
    tags = {
        @Tag(name = "listing", description = "listing data operations"),
        @Tag(name = "player", description = "player ranking data"),
        @Tag(name = "club", description = "club ranking players"),
        @Tag(name = "status", description = "application data status")
    })
@Configuration
public class OpenApiConfig {

}
