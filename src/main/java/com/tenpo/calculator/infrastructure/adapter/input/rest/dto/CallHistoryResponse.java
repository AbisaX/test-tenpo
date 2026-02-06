package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import com.tenpo.calculator.domain.model.CallHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Registro del historial de llamadas")
public record CallHistoryResponse(
    @Schema(description = "Identificador único", example = "1")
    Long id,

    @Schema(description = "Fecha y hora de la llamada", example = "2024-01-15T10:30:00")
    LocalDateTime timestamp,

    @Schema(description = "Endpoint invocado", example = "/api/v1/calculator/calculate")
    String endpoint,

    @Schema(description = "Método HTTP utilizado", example = "POST")
    String httpMethod,

    @Schema(description = "Parámetros recibidos", example = "{\"num1\": 5, \"num2\": 5}")
    String parameters,

    @Schema(description = "Respuesta retornada o mensaje de error", example = "{\"result\": 11}")
    String response,

    @Schema(description = "Código de estado HTTP", example = "200")
    Integer statusCode,

    @Schema(description = "Indica si la llamada fue exitosa", example = "true")
    boolean success
) {
    public static CallHistoryResponse fromDomain(CallHistory historialLlamada) {
        return new CallHistoryResponse(
            historialLlamada.id(),
            historialLlamada.timestamp(),
            historialLlamada.endpoint(),
            historialLlamada.httpMethod(),
            historialLlamada.parameters(),
            historialLlamada.response(),
            historialLlamada.statusCode(),
            historialLlamada.success()
        );
    }
}
