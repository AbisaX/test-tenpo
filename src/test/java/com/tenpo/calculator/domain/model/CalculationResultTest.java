package com.tenpo.calculator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CalculationResult - Tests unitarios")
class CalculationResultTest {

    @Test
    @DisplayName("Debe calcular el resultado correctamente con porcentaje del 10%")
    void debeCalcularResultadoConDiezPorciento() {
        BigDecimal num1 = new BigDecimal("5");
        BigDecimal num2 = new BigDecimal("5");
        BigDecimal porcentaje = new BigDecimal("10");

        CalculationResult resultado = CalculationResult.of(num1, num2, porcentaje);

        assertThat(resultado.num1()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(resultado.num2()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(resultado.percentage()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("11"));
    }

    @Test
    @DisplayName("Debe calcular el resultado correctamente con porcentaje del 0%")
    void debeCalcularResultadoConCeroPorciento() {
        BigDecimal num1 = new BigDecimal("100");
        BigDecimal num2 = new BigDecimal("50");
        BigDecimal porcentaje = BigDecimal.ZERO;

        CalculationResult resultado = CalculationResult.of(num1, num2, porcentaje);

        assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("150"));
        assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("150"));
    }

    @Test
    @DisplayName("Debe calcular el resultado correctamente con números decimales")
    void debeCalcularResultadoConNumerosDecimales() {
        BigDecimal num1 = new BigDecimal("10.5");
        BigDecimal num2 = new BigDecimal("5.5");
        BigDecimal porcentaje = new BigDecimal("20");

        CalculationResult resultado = CalculationResult.of(num1, num2, porcentaje);

        assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("16"));
        assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("19.2"));
    }

    @Test
    @DisplayName("Debe calcular el resultado correctamente con números negativos")
    void debeCalcularResultadoConNumerosNegativos() {
        BigDecimal num1 = new BigDecimal("-5");
        BigDecimal num2 = new BigDecimal("10");
        BigDecimal porcentaje = new BigDecimal("10");

        CalculationResult resultado = CalculationResult.of(num1, num2, porcentaje);

        assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("5.5"));
    }

    @Test
    @DisplayName("Debe calcular el resultado correctamente con números grandes")
    void debeCalcularResultadoConNumerosGrandes() {
        BigDecimal num1 = new BigDecimal("1000000");
        BigDecimal num2 = new BigDecimal("2000000");
        BigDecimal porcentaje = new BigDecimal("15");

        CalculationResult resultado = CalculationResult.of(num1, num2, porcentaje);

        assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("3000000"));
        assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("3450000"));
    }
}
