package com.Catalogo.Inventario.config;

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
                title = "API de Inventario de Productos",
                version = "1.0.0",
                description = "API REST para gestión de catálogo de productos, stock e inventario del sistema PcOneStop. " +
                        "Algunos endpoints requieren autenticación JWT con rol de administrador.",
                contact = @Contact(
                        name = "PcOneStop",
                        email = "support@pconestop.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8082", description = "Servidor de desarrollo"),
                @Server(url = "http://localhost:8082", description = "Servidor local")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Autenticación JWT. Obtén el token desde el microservicio de Usuarios (/api/v1/auth/login). " +
                "Los endpoints de administración requieren rol ADMIN."
)
public class OpenApiConfig {
}

