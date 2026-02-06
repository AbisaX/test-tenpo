package com.tenpo.calculator.infrastructure.adapter.input.rest;

import com.tenpo.calculator.domain.exception.ExternalServiceException;
import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.input.CalculatorUseCase;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.CalculationRequest;
import com.tenpo.calculator.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculatorController - Tests Unitarios")
class CalculatorControllerTest {

    @Mock
    private CalculatorUseCase calculatorUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void configurar() {
        CalculatorController controller = new CalculatorController(calculatorUseCase);
        webTestClient = WebTestClient.bindToController(controller)
            .controllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("Debe retornar resultado del cálculo con estado 200 OK")
    void debeRetornarResultadoDelCalculo() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("5");
        BigDecimal num2 = new BigDecimal("5");
        CalculationResult resultado = CalculationResult.of(num1, num2, new BigDecimal("10"));

        when(calculatorUseCase.calculateWithPercentage(any(), any()))
            .thenReturn(Mono.just(resultado));

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new CalculationRequest(num1, num2))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.num1").isEqualTo(5)
            .jsonPath("$.num2").isEqualTo(5)
            .jsonPath("$.sum").isEqualTo(10)
            .jsonPath("$.percentageApplied").isEqualTo(10)
            .jsonPath("$.result").isEqualTo(11);

        verify(calculatorUseCase).calculateWithPercentage(any(), any());
    }

    @Test
    @DisplayName("Debe retornar 503 cuando el servicio externo falla")
    void debeRetornar503CuandoServicioExternoFalla() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("5");
        BigDecimal num2 = new BigDecimal("5");

        when(calculatorUseCase.calculateWithPercentage(any(), any()))
            .thenReturn(Mono.error(new ExternalServiceException("Servicio no disponible después de reintentos")));

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new CalculationRequest(num1, num2))
            .exchange()
            .expectStatus().isEqualTo(503)
            .expectBody()
            .jsonPath("$.status").isEqualTo(503)
            .jsonPath("$.error").isEqualTo("Servicio No Disponible");
    }

    @Test
    @DisplayName("Debe retornar 400 cuando num1 es nulo")
    void debeRetornar400CuandoNum1EsNulo() {
        // Arrange (Preparación) - sin preparación necesaria

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"num2\": 5}")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Debe retornar 400 cuando num2 es nulo")
    void debeRetornar400CuandoNum2EsNulo() {
        // Arrange (Preparación) - sin preparación necesaria

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"num1\": 5}")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Debe manejar números decimales correctamente")
    void debeManejarNumerosDecimales() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("10.5");
        BigDecimal num2 = new BigDecimal("5.5");
        CalculationResult resultado = CalculationResult.of(num1, num2, new BigDecimal("20"));

        when(calculatorUseCase.calculateWithPercentage(any(), any()))
            .thenReturn(Mono.just(resultado));

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new CalculationRequest(num1, num2))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.sum").isEqualTo(16)
            .jsonPath("$.result").isEqualTo(19.2);
    }

    @Test
    @DisplayName("Debe manejar números negativos correctamente")
    void debeManejarNumerosNegativos() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("-5");
        BigDecimal num2 = new BigDecimal("10");
        CalculationResult resultado = CalculationResult.of(num1, num2, new BigDecimal("10"));

        when(calculatorUseCase.calculateWithPercentage(any(), any()))
            .thenReturn(Mono.just(resultado));

        // Act & Assert (Acción y Verificación)
        webTestClient.post()
            .uri("/api/v1/calculator/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new CalculationRequest(num1, num2))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.sum").isEqualTo(5)
            .jsonPath("$.result").isEqualTo(5.5);
    }
}
