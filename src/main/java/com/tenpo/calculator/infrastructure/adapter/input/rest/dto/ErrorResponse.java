package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta de error")
public record ErrorResponse(
    @Schema(description = "Fecha y hora cuando ocurrió el error", example = "2024-01-15T10:30:00")
    LocalDateTime timestamp,

    @Schema(description = "Código de estado HTTP", example = "429")
    int status,

    @Schema(description = "Tipo de error", example = "Demasiadas Peticiones")
    String error,

    @Schema(description = "Mensaje detallado del error", example = "Límite de peticiones excedido. Máximo 3 por minuto.")
    String message,

    @Schema(description = "Ruta de la petición que causó el error", example = "/api/v1/calculator/calculate")
    String path
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path);
    }
}
