package com.tenpo.calculator.integration;

import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.CalculationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("API Calculadora - Tests de Integración")
class CalculatorIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("calculator_db")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
            "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/calculator_db");
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("external-service.mock.enabled", () -> "true");
        registry.add("rate-limit.requests-per-minute", () -> "100"); // Límite mayor para tests
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Debe calcular suma con porcentaje exitosamente")
    void debeCalcularSumaConPorcentaje() {
        // Arrange (Preparación)
        CalculationRequest peticion = new CalculationRequest(
            new BigDecimal("5"),
            new BigDecimal("5")
        );

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(peticion)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.num1").isEqualTo(5)
            .jsonPath("$.num2").isEqualTo(5)
            .jsonPath("$.sum").isEqualTo(10)
            .jsonPath("$.percentageApplied").isEqualTo(10)
            .jsonPath("$.result").isEqualTo(11);
    }

    @Test
    @DisplayName("Debe retornar solicitud incorrecta para parámetros faltantes")
    void debeRetornarSolicitudIncorrectaParaParametrosFaltantes() {
        // Arrange (Preparación) - petición vacía

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Debe obtener historial de llamadas con paginación")
    void debeObtenerHistorialConPaginacion() {
        // Arrange (Preparación) - Primero hacer un cálculo
        CalculationRequest peticion = new CalculationRequest(
            new BigDecimal("10"),
            new BigDecimal("20")
        );

        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(peticion)
            .exchange()
            .expectStatus().isOk();

        // Esperar guardado asíncrono del historial
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert (Acción y Verificación) - Obtener historial
        webTestClient.get()
            .uri("/api/v1/history?page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content").isArray()
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(10);
    }

    @Test
    @DisplayName("Debe manejar números decimales en el cálculo")
    void debeManejarNumerosDecimales() {
        // Arrange (Preparación)
        CalculationRequest peticion = new CalculationRequest(
            new BigDecimal("10.5"),
            new BigDecimal("5.5")
        );

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(peticion)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.sum").isEqualTo(16)
            .jsonPath("$.result").isEqualTo(17.6);
    }

    @Test
    @DisplayName("Debe manejar números negativos")
    void debeManejarNumerosNegativos() {
        // Arrange (Preparación)
        CalculationRequest peticion = new CalculationRequest(
            new BigDecimal("-5"),
            new BigDecimal("15")
        );

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(peticion)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.sum").isEqualTo(10)
            .jsonPath("$.result").isEqualTo(11);
    }

    @Test
    @DisplayName("Swagger UI debe ser accesible")
    void swaggerUiDebeSerAccesible() {
        // Arrange (Preparación) - no se requiere

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/swagger-ui.html")
            .exchange()
            .expectStatus().is3xxRedirection();
    }

    @Test
    @DisplayName("Documentación de API debe ser accesible")
    void documentacionApiDebeSerAccesible() {
        // Arrange (Preparación) - no se requiere

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/api-docs")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @DisplayName("Endpoint de salud debe ser accesible")
    void endpointDeSaludDebeSerAccesible() {
        // Arrange (Preparación) - no se requiere

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk();
    }
}
