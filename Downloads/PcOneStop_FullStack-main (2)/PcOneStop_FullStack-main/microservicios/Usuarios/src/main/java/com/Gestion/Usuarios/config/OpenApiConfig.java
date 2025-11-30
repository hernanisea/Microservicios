package com.Gestion.Usuarios.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Gestión de Usuarios",
                version = "1.0.0",
                description = "API REST para autenticación y gestión de usuarios del sistema PcOneStop. " +
                        "Incluye registro, login con JWT, y gestión de cuentas de usuario.",
                contact = @Contact(
                        name = "PcOneStop",
                        email = "support@pconestop.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Servidor de desarrollo"),
                @Server(url = "http://localhost:8081", description = "Servidor local")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Autenticación JWT. Obtén el token haciendo login en el endpoint /api/v1/auth/login"
)
public class OpenApiConfig {
}

