package com.actdigital.lojaonlinepedidobe.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "Pedido Service API",
                version     = "v1",
                description = "Gerencia criação e atualização de pedidos"
        ),
        servers = @Server(url = "/", description = "Servidor local")
)
public class OpenApiConfig { }
