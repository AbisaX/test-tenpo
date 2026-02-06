package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse - Tests unitarios")
class ErrorResponseTest {

    @Test
    @DisplayName("Debe crear ErrorResponse con método de fábrica")
    void debeCrearConMetodoDeFabrica() {
        ErrorResponse respuesta = ErrorResponse.of(
            429,
            "Demasiadas peticiones",
            "Límite de peticiones excedido",
            "/api/v1/calculator/calculate"
        );

        assertThat(respuesta.status()).isEqualTo(429);
        assertThat(respuesta.error()).isEqualTo("Demasiadas peticiones");
        assertThat(respuesta.message()).isEqualTo("Límite de peticiones excedido");
        assertThat(respuesta.path()).isEqualTo("/api/v1/calculator/calculate");
        assertThat(respuesta.timestamp()).isNotNull();
        assertThat(respuesta.timestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Debe crear ErrorResponse para diferentes tipos de error")
    void debeCrearParaDiferentesTiposDeError() {
        ErrorResponse solicitudInvalida = ErrorResponse.of(
            400,
            "Solicitud inválida",
            "Entrada inválida",
            "/api/v1/test"
        );
        assertThat(solicitudInvalida.status()).isEqualTo(400);

        ErrorResponse errorServidor = ErrorResponse.of(
            500,
            "Error interno del servidor",
            "Error inesperado",
            "/api/v1/test"
        );
        assertThat(errorServidor.status()).isEqualTo(500);

        ErrorResponse servicioNoDisponible = ErrorResponse.of(
            503,
            "Servicio no disponible",
            "Servicio externo caído",
            "/api/v1/test"
        );
        assertThat(servicioNoDisponible.status()).isEqualTo(503);
    }
}
