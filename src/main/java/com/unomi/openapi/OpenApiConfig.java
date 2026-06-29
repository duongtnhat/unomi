package com.unomi.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SECURITY_SCHEME = "apiKeyAuth";

    @Bean
    OpenAPI unomiOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Unomi Modern API")
                .version("0.0.1")
                .description("""
                    Customer data platform API built with Spring Boot.

                    Profiles and events are stored in Elasticsearch. Definitions and API keys are stored in PostgreSQL.
                    Every business API under /api/** requires an API key in the X-API-Key header.
                    """)
                .contact(new Contact().name("Unomi Modern")))
            .components(new Components().addSecuritySchemes(API_KEY_SECURITY_SCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-Key")
                    .description("Use the local development key: dev-unomi-api-key")))
            .addSecurityItem(new SecurityRequirement().addList(API_KEY_SECURITY_SCHEME));
    }
}
