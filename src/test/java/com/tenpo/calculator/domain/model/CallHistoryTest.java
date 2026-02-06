package com.tenpo.calculator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CallHistory - Tests Unitarios")
class CallHistoryTest {

    @Test
    @DisplayName("Debe crear CallHistory con valores correctos")
    void debeCrearCallHistoryConValoresCorrectos() {
        // Arrange (Preparación)
        String endpoint = "/api/v1/calculator/calculate";
        String metodoHttp = "POST";
        String parametros = "{\"num1\": 5, \"num2\": 5}";
        String respuesta = "{\"result\": 11}";
        Integer codigoEstado = 200;
        boolean exitoso = true;

        // Act (Acción)
        CallHistory historial = CallHistory.create(endpoint, metodoHttp, parametros, respuesta, codigoEstado, exitoso);

        // Assert (Verificación)
        assertThat(historial.id()).isNull();
        assertThat(historial.timestamp()).isNotNull();
        assertThat(historial.timestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(historial.endpoint()).isEqualTo(endpoint);
        assertThat(historial.httpMethod()).isEqualTo(metodoHttp);
        assertThat(historial.parameters()).isEqualTo(parametros);
        assertThat(historial.response()).isEqualTo(respuesta);
        assertThat(historial.statusCode()).isEqualTo(codigoEstado);
        assertThat(historial.success()).isTrue();
    }

    @Test
    @DisplayName("Debe crear CallHistory para petición fallida")
    void debeCrearCallHistoryParaPeticionFallida() {
        // Arrange (Preparación)
        String endpoint = "/api/v1/calculator/calculate";
        String metodoHttp = "POST";
        String parametros = "{}";
        String respuesta = "{\"error\": \"Solicitud Incorrecta\"}";
        Integer codigoEstado = 400;
        boolean exitoso = false;

        // Act (Acción)
        CallHistory historial = CallHistory.create(endpoint, metodoHttp, parametros, respuesta, codigoEstado, exitoso);

        // Assert (Verificación)
        assertThat(historial.success()).isFalse();
        assertThat(historial.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Debe crear CallHistory mediante constructor del record")
    void debeCrearCallHistoryMedianteConstructor() {
        // Arrange (Preparación)
        Long id = 1L;
        LocalDateTime timestamp = LocalDateTime.now();
        String endpoint = "/api/v1/history";
        String metodoHttp = "GET";
        String parametros = "{}";
        String respuesta = "[]";
        Integer codigoEstado = 200;
        boolean exitoso = true;

        // Act (Acción)
        CallHistory historial = new CallHistory(id, timestamp, endpoint, metodoHttp, parametros, respuesta, codigoEstado, exitoso);

        // Assert (Verificación)
        assertThat(historial.id()).isEqualTo(id);
        assertThat(historial.timestamp()).isEqualTo(timestamp);
        assertThat(historial.endpoint()).isEqualTo(endpoint);
    }
}
