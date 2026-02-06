package com.tenpo.calculator.infrastructure.adapter.output.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MockPercentageServiceAdapter - Tests Unitarios")
class MockPercentageServiceAdapterTest {

    private MockPercentageServiceAdapter adapter;

    @BeforeEach
    void configurar() {
        adapter = new MockPercentageServiceAdapter();
    }

    @Test
    @DisplayName("Debe retornar porcentaje mock del 10%")
    void debeRetornarPorcentajeMock() {
        // Arrange (Preparación) - no se requiere preparación adicional

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(adapter.getPercentage())
            .assertNext(porcentaje -> {
                assertThat(porcentaje).isEqualByComparingTo(new BigDecimal("10"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar valor de porcentaje consistente en múltiples llamadas")
    void debeRetornarValorConsistente() {
        // Arrange (Preparación) - no se requiere preparación adicional

        // Act & Assert (Acción y Verificación) - llamar múltiples veces
        StepVerifier.create(adapter.getPercentage())
            .assertNext(p1 -> assertThat(p1).isEqualByComparingTo(new BigDecimal("10")))
            .verifyComplete();

        StepVerifier.create(adapter.getPercentage())
            .assertNext(p2 -> assertThat(p2).isEqualByComparingTo(new BigDecimal("10")))
            .verifyComplete();
    }
}
