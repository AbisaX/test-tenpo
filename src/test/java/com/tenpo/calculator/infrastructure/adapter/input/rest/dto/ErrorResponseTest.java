package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse - Tests Unitarios")
class ErrorResponseTest {

    @Test
    @DisplayName("Debe crear ErrorResponse con método de fábrica")
    void debeCrearConMetodoDeFabrica() {
        // Arrange (Preparación) - no se requiere

        // Act (Acción)
        ErrorResponse respuesta = ErrorResponse.of(
            429,
            "Demasiadas Peticiones",
            "Límite de peticiones excedido",
            "/api/v1/calculator/calculate"
        );

        // Assert (Verificación)
        assertThat(respuesta.status()).isEqualTo(429);
        assertThat(respuesta.error()).isEqualTo("Demasiadas Peticiones");
        assertThat(respuesta.message()).isEqualTo("Límite de peticiones excedido");
        assertThat(respuesta.path()).isEqualTo("/api/v1/calculator/calculate");
        assertThat(respuesta.timestamp()).isNotNull();
        assertThat(respuesta.timestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Debe crear ErrorResponse para diferentes tipos de error")
    void debeCrearParaDiferentesTiposDeError() {
        // Arrange (Preparación) - no se requiere

        // Act & Assert (Acción y Verificación)

        // 400 Solicitud Incorrecta
        ErrorResponse solicitudIncorrecta = ErrorResponse.of(400, "Solicitud Incorrecta", "Entrada inválida", "/api/v1/test");
        assertThat(solicitudIncorrecta.status()).isEqualTo(400);

        // 500 Error Interno del Servidor
        ErrorResponse errorServidor = ErrorResponse.of(500, "Error Interno del Servidor", "Error inesperado", "/api/v1/test");
        assertThat(errorServidor.status()).isEqualTo(500);

        // 503 Servicio No Disponible
        ErrorResponse servicioNoDisponible = ErrorResponse.of(503, "Servicio No Disponible", "Servicio externo caído", "/api/v1/test");
        assertThat(servicioNoDisponible.status()).isEqualTo(503);
    }
}
