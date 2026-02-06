package com.tenpo.calculator.infrastructure.adapter.output.persistence.entity;

import com.tenpo.calculator.domain.model.CallHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CallHistoryEntity - Tests unitarios")
class CallHistoryEntityTest {

    @Test
    @DisplayName("Debe convertir de modelo de dominio a entidad")
    void debeConvertirDeDominioAEntidad() {
        CallHistory historial = new CallHistory(
            1L,
            LocalDateTime.now(),
            "/api/v1/calculator/calculate",
            "POST",
            "{\"num1\": 5, \"num2\": 5}",
            "{\"result\": 11}",
            200,
            true
        );

        CallHistoryEntity entidad = CallHistoryEntity.fromDomain(historial);

        assertThat(entidad.getId()).isEqualTo(historial.id());
        assertThat(entidad.getTimestamp()).isEqualTo(historial.timestamp());
        assertThat(entidad.getEndpoint()).isEqualTo(historial.endpoint());
        assertThat(entidad.getHttpMethod()).isEqualTo(historial.httpMethod());
        assertThat(entidad.getParameters()).isEqualTo(historial.parameters());
        assertThat(entidad.getResponse()).isEqualTo(historial.response());
        assertThat(entidad.getStatusCode()).isEqualTo(historial.statusCode());
        assertThat(entidad.isSuccess()).isEqualTo(historial.success());
    }

    @Test
    @DisplayName("Debe convertir de entidad a modelo de dominio")
    void debeConvertirDeEntidadADominio() {
        CallHistoryEntity entidad = CallHistoryEntity.builder()
            .id(1L)
            .timestamp(LocalDateTime.now())
            .endpoint("/api/v1/calculator/calculate")
            .httpMethod("POST")
            .parameters("{\"num1\": 5}")
            .response("{\"result\": 11}")
            .statusCode(200)
            .success(true)
            .build();

        CallHistory dominio = entidad.toDomain();

        assertThat(dominio.id()).isEqualTo(entidad.getId());
        assertThat(dominio.timestamp()).isEqualTo(entidad.getTimestamp());
        assertThat(dominio.endpoint()).isEqualTo(entidad.getEndpoint());
        assertThat(dominio.httpMethod()).isEqualTo(entidad.getHttpMethod());
        assertThat(dominio.parameters()).isEqualTo(entidad.getParameters());
        assertThat(dominio.response()).isEqualTo(entidad.getResponse());
        assertThat(dominio.statusCode()).isEqualTo(entidad.getStatusCode());
        assertThat(dominio.success()).isEqualTo(entidad.isSuccess());
    }

    @Test
    @DisplayName("Debe manejar valores nulos correctamente")
    void debeManejarValoresNulos() {
        CallHistory historial = new CallHistory(
            null,
            LocalDateTime.now(),
            "/api/v1/calculator/calculate",
            "POST",
            null,
            null,
            null,
            false
        );

        CallHistoryEntity entidad = CallHistoryEntity.fromDomain(historial);
        CallHistory deVueltaADominio = entidad.toDomain();

        assertThat(deVueltaADominio.id()).isNull();
        assertThat(deVueltaADominio.parameters()).isNull();
        assertThat(deVueltaADominio.response()).isNull();
        assertThat(deVueltaADominio.statusCode()).isNull();
    }
}
