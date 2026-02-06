package com.tenpo.calculator.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Calculadora - Desafío Tenpo")
                .description("""
                    API REST para cálculos con porcentaje dinámico.

                    ## Características
                    - **Cálculo con porcentaje**: Suma dos números y aplica un porcentaje obtenido de un servicio externo
                    - **Historial de llamadas**: Consulta paginada de todas las llamadas realizadas a la API
                    - **Límite de peticiones**: Máximo 3 solicitudes por minuto
                    - **Reintentos automáticos**: Reintentos ante fallos del servicio externo (máximo 3 intentos)

                    ## Códigos de error
                    | Código | Descripción |
                    |--------|-------------|
                    | 400 | Parámetros de entrada inválidos |
                    | 429 | Límite de peticiones excedido |
                    | 503 | Servicio externo no disponible |

                    ## Ejemplo de uso
                    ```json
                    POST /api/v1/calculator/calculate
                    {
                      "num1": 5,
                      "num2": 5
                    }

                    Respuesta: (5 + 5) + 10% = 11
                    ```
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Desafío Técnico Tenpo")
                    .email("dev@tenpo.cl"))
                .license(new License()
                    .name("Licencia MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor de desarrollo local")
            ));
    }
}
