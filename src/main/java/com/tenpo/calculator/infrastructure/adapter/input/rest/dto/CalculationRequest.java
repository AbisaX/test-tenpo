package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Solicitud para cálculo con porcentaje")
public record CalculationRequest(
    @NotNull(message = "num1 es requerido")
    @Schema(description = "Primer número para el cálculo", example = "5")
    BigDecimal num1,

    @NotNull(message = "num2 es requerido")
    @Schema(description = "Segundo número para el cálculo", example = "5")
    BigDecimal num2
) {}
