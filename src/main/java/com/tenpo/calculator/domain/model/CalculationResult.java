package com.tenpo.calculator.domain.model;

import java.math.BigDecimal;

public record CalculationResult(
    BigDecimal num1,
    BigDecimal num2,
    BigDecimal sum,
    BigDecimal percentage,
    BigDecimal result
) {
    public static CalculationResult of(BigDecimal num1, BigDecimal num2, BigDecimal percentage) {
        BigDecimal sum = num1.add(num2);
        BigDecimal percentageValue = sum.multiply(percentage).divide(BigDecimal.valueOf(100));
        BigDecimal result = sum.add(percentageValue);
        return new CalculationResult(num1, num2, sum, percentage, result);
    }
}
