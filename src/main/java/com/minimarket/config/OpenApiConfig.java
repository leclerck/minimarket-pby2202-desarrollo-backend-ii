package com.minimarket.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la documentación OpenAPI 3 (Swagger UI).
 *
 * Define el esquema de seguridad Bearer JWT que habilita el botón "Authorize"
 * en Swagger UI. La clave "bearerAuth" es referenciada por @SecurityRequirement
 * en cada controlador o método que requiera autenticación.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Minimarket API",
                version = "1.1",
                description = "API REST para la gestión de un minimarket. " +
                        "Autenticación stateless con JWT. Roles: ADMIN y CAJERO."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresa el token JWT obtenido en POST /api/auth/login"
)
public class OpenApiConfig {
}
