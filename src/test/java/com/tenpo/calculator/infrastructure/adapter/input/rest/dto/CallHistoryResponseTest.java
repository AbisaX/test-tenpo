package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import com.tenpo.calculator.domain.model.CallHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CallHistoryResponse - Tests Unitarios")
class CallHistoryResponseTest {

    @Test
    @DisplayName("Debe crear CallHistoryResponse desde modelo de dominio")
    void debeCrearDesdeModeloDeDominio() {
        // Arrange (Preparación)
        LocalDateTime timestamp = LocalDateTime.now();
        CallHistory dominio = new CallHistory(
            1L,
            timestamp,
            "/api/v1/calculator/calculate",
            "POST",
            "{\"num1\": 5}",
            "{\"result\": 11}",
            200,
            true
        );

        // Act (Acción)
        CallHistoryResponse respuesta = CallHistoryResponse.fromDomain(dominio);

        // Assert (Verificación)
        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.timestamp()).isEqualTo(timestamp);
        assertThat(respuesta.endpoint()).isEqualTo("/api/v1/calculator/calculate");
        assertThat(respuesta.httpMethod()).isEqualTo("POST");
        assertThat(respuesta.parameters()).isEqualTo("{\"num1\": 5}");
        assertThat(respuesta.response()).isEqualTo("{\"result\": 11}");
        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.success()).isTrue();
    }

    @Test
    @DisplayName("Debe manejar respuesta de petición fallida")
    void debeManejarPeticionFallida() {
        // Arrange (Preparación)
        CallHistory dominio = new CallHistory(
            2L,
            LocalDateTime.now(),
            "/api/v1/calculator/calculate",
            "POST",
            "{}",
            "{\"error\": \"Solicitud Incorrecta\"}",
            400,
            false
        );

        // Act (Acción)
        CallHistoryResponse respuesta = CallHistoryResponse.fromDomain(dominio);

        // Assert (Verificación)
        assertThat(respuesta.statusCode()).isEqualTo(400);
        assertThat(respuesta.success()).isFalse();
    }
}
