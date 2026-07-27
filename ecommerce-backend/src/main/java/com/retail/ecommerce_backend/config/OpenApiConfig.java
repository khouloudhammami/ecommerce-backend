package com.retail.ecommerce_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "E-Commerce Retail API",
        version = "1.0",
        description = "API pour la gestion des commandes, stocks et authentification."
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Environnement de développement")
       
    },
    
    security = @SecurityRequirement(name = "bearerAuth")
)
//C'est ce qui faisait apparaître le bouton Authorize dans Swagger
@SecurityScheme(
    name = "bearerAuth", // Le nom utilisé dans Swagger
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER // Le token sera dans le header
)
public class OpenApiConfig {
    // Cette classe ne contient que des annotations, pas besoin de code métier.
}
