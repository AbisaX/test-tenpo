package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import com.tenpo.calculator.domain.model.CalculationResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Respuesta con el resultado del cálculo y porcentaje aplicado")
public record CalculationResponse(
    @Schema(description = "Primer número", example = "5")
    BigDecimal num1,

    @Schema(description = "Segundo número", example = "5")
    BigDecimal num2,

    @Schema(description = "Suma de num1 y num2", example = "10")
    BigDecimal sum,

    @Schema(description = "Porcentaje aplicado (obtenido del servicio externo)", example = "10")
    BigDecimal percentageApplied,

    @Schema(description = "Resultado final después de aplicar el porcentaje", example = "11")
    BigDecimal result
) {
    public static CalculationResponse fromDomain(CalculationResult resultadoCalculo) {
        return new CalculationResponse(
            resultadoCalculo.num1(),
            resultadoCalculo.num2(),
            resultadoCalculo.sum(),
            resultadoCalculo.percentage(),
            resultadoCalculo.result()
        );
    }
}
