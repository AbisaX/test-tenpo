package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import com.tenpo.calculator.domain.model.CalculationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CalculationResponse - Tests Unitarios")
class CalculationResponseTest {

    @Test
    @DisplayName("Debe crear CalculationResponse desde modelo de dominio")
    void debeCrearDesdeModeloDeDominio() {
        // Arrange (Preparación)
        CalculationResult dominio = CalculationResult.of(
            new BigDecimal("5"),
            new BigDecimal("5"),
            new BigDecimal("10")
        );

        // Act (Acción)
        CalculationResponse respuesta = CalculationResponse.fromDomain(dominio);

        // Assert (Verificación)
        assertThat(respuesta.num1()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(respuesta.num2()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(respuesta.sum()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(respuesta.percentageApplied()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(respuesta.result()).isEqualByComparingTo(new BigDecimal("11"));
    }

    @Test
    @DisplayName("Debe mapear todos los campos correctamente")
    void debeMapearTodosLosCampos() {
        // Arrange (Preparación)
        CalculationResult dominio = CalculationResult.of(
            new BigDecimal("100"),
            new BigDecimal("200"),
            new BigDecimal("25")
        );

        // Act (Acción)
        CalculationResponse respuesta = CalculationResponse.fromDomain(dominio);

        // Assert (Verificación)
        assertThat(respuesta.sum()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(respuesta.result()).isEqualByComparingTo(new BigDecimal("375"));
    }
}
