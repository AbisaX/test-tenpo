package com.tenpo.calculator.infrastructure.exception;

import com.tenpo.calculator.domain.exception.ExternalServiceException;
import com.tenpo.calculator.domain.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler - Tests Unitarios")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void configurar() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Debe manejar RateLimitExceededException con estado 429")
    void debeManejarRateLimitExceededException() {
        // Arrange (Preparación)
        RateLimitExceededException excepcion = new RateLimitExceededException("Límite de peticiones excedido");
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/calculator/calculate").build()
        );

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(exceptionHandler.handleRateLimitExceeded(excepcion, exchange))
            .assertNext(respuesta -> {
                assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                assertThat(respuesta.getBody()).isNotNull();
                assertThat(respuesta.getBody().status()).isEqualTo(429);
                assertThat(respuesta.getBody().error()).isEqualTo("Demasiadas Peticiones");
                assertThat(respuesta.getBody().message()).isEqualTo("Límite de peticiones excedido. Máximo 3 peticiones por minuto permitidas. Por favor, intente nuevamente más tarde.");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar ExternalServiceException con estado 503")
    void debeManejarExternalServiceException() {
        // Arrange (Preparación)
        ExternalServiceException excepcion = new ExternalServiceException("Servicio no disponible");
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/calculator/calculate").build()
        );

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(exceptionHandler.handleExternalServiceException(excepcion, exchange))
            .assertNext(respuesta -> {
                assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                assertThat(respuesta.getBody()).isNotNull();
                assertThat(respuesta.getBody().status()).isEqualTo(503);
                assertThat(respuesta.getBody().error()).isEqualTo("Servicio No Disponible");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar IllegalArgumentException con estado 400")
    void debeManejarIllegalArgumentException() {
        // Arrange (Preparación)
        IllegalArgumentException excepcion = new IllegalArgumentException("Parámetro inválido");
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/calculator/calculate").build()
        );

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(exceptionHandler.handleIllegalArgument(excepcion, exchange))
            .assertNext(respuesta -> {
                assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(respuesta.getBody()).isNotNull();
                assertThat(respuesta.getBody().status()).isEqualTo(400);
                assertThat(respuesta.getBody().error()).isEqualTo("Solicitud Incorrecta");
                assertThat(respuesta.getBody().message()).isEqualTo("Los parámetros proporcionados son inválidos: Parámetro inválido");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar Exception genérica con estado 500")
    void debeManejarExceptionGenerica() {
        // Arrange (Preparación)
        Exception excepcion = new Exception("Error inesperado");
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/v1/calculator/calculate").build()
        );

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(exceptionHandler.handleGenericException(excepcion, exchange))
            .assertNext(respuesta -> {
                assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                assertThat(respuesta.getBody()).isNotNull();
                assertThat(respuesta.getBody().status()).isEqualTo(500);
                assertThat(respuesta.getBody().error()).isEqualTo("Error Interno del Servidor");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe incluir la ruta en la respuesta de error")
    void debeIncluirRutaEnRespuestaDeError() {
        // Arrange (Preparación)
        RateLimitExceededException excepcion = new RateLimitExceededException("Límite de peticiones excedido");
        String ruta = "/api/v1/calculator/calculate";
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post(ruta).build()
        );

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(exceptionHandler.handleRateLimitExceeded(excepcion, exchange))
            .assertNext(respuesta -> {
                assertThat(respuesta.getBody()).isNotNull();
                assertThat(respuesta.getBody().path()).isEqualTo(ruta);
            })
            .verifyComplete();
    }
}
