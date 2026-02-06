package com.tenpo.calculator.domain.model;

import java.math.BigDecimal;

public record CalculationResult(
    BigDecimal num1,
    BigDecimal num2,
    BigDecimal sum,
    BigDecimal percentage,
    BigDecimal result
) {
    public static CalculationResult of(BigDecimal num1, BigDecimal num2, BigDecimal porcentaje) {
        BigDecimal suma = num1.add(num2);
        BigDecimal incremento = suma.multiply(porcentaje).divide(BigDecimal.valueOf(100));
        BigDecimal resultado = suma.add(incremento);
        return new CalculationResult(num1, num2, suma, porcentaje, resultado);
    }
}
