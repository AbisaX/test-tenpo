package com.tenpo.calculator.application.service;

import com.tenpo.calculator.domain.exception.ExternalServiceException;
import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.output.PercentageServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculatorService - Tests Unitarios")
class CalculatorServiceTest {

    @Mock
    private PercentageServicePort percentageServicePort;

    private CalculatorService calculatorService;

    @BeforeEach
    void configurar() {
        calculatorService = new CalculatorService(percentageServicePort);
    }

    @Test
    @DisplayName("Debe calcular la suma con porcentaje del servicio externo")
    void debeCalcularSumaConPorcentaje() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("5");
        BigDecimal num2 = new BigDecimal("5");
        BigDecimal porcentaje = new BigDecimal("10");

        when(percentageServicePort.getPercentage()).thenReturn(Mono.just(porcentaje));

        // Act (Acción)
        Mono<CalculationResult> resultadoMono = calculatorService.calculateWithPercentage(num1, num2);

        // Assert (Verificación)
        StepVerifier.create(resultadoMono)
            .assertNext(resultado -> {
                assertThat(resultado.num1()).isEqualByComparingTo(num1);
                assertThat(resultado.num2()).isEqualByComparingTo(num2);
                assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("10"));
                assertThat(resultado.percentage()).isEqualByComparingTo(porcentaje);
                assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("11"));
            })
            .verifyComplete();

        verify(percentageServicePort, times(1)).getPercentage();
    }

    @Test
    @DisplayName("Debe manejar porcentaje cero correctamente")
    void debeManejarPorcentajeCero() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("100");
        BigDecimal num2 = new BigDecimal("200");
        BigDecimal porcentaje = BigDecimal.ZERO;

        when(percentageServicePort.getPercentage()).thenReturn(Mono.just(porcentaje));

        // Act (Acción)
        Mono<CalculationResult> resultadoMono = calculatorService.calculateWithPercentage(num1, num2);

        // Assert (Verificación)
        StepVerifier.create(resultadoMono)
            .assertNext(resultado -> {
                assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("300"));
                assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("300"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe propagar error cuando el servicio externo falla")
    void debePropararErrorCuandoServicioFalla() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("5");
        BigDecimal num2 = new BigDecimal("5");

        when(percentageServicePort.getPercentage())
            .thenReturn(Mono.error(new ExternalServiceException("Servicio no disponible")));

        // Act (Acción)
        Mono<CalculationResult> resultadoMono = calculatorService.calculateWithPercentage(num1, num2);

        // Assert (Verificación)
        StepVerifier.create(resultadoMono)
            .expectError(ExternalServiceException.class)
            .verify();

        verify(percentageServicePort, times(1)).getPercentage();
    }

    @Test
    @DisplayName("Debe manejar números decimales correctamente")
    void debeManejarNumerosDecimales() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("10.5");
        BigDecimal num2 = new BigDecimal("5.5");
        BigDecimal porcentaje = new BigDecimal("25");

        when(percentageServicePort.getPercentage()).thenReturn(Mono.just(porcentaje));

        // Act (Acción)
        Mono<CalculationResult> resultadoMono = calculatorService.calculateWithPercentage(num1, num2);

        // Assert (Verificación)
        StepVerifier.create(resultadoMono)
            .assertNext(resultado -> {
                assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("16"));
                assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("20"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar números negativos correctamente")
    void debeManejarNumerosNegativos() {
        // Arrange (Preparación)
        BigDecimal num1 = new BigDecimal("-10");
        BigDecimal num2 = new BigDecimal("20");
        BigDecimal porcentaje = new BigDecimal("50");

        when(percentageServicePort.getPercentage()).thenReturn(Mono.just(porcentaje));

        // Act (Acción)
        Mono<CalculationResult> resultadoMono = calculatorService.calculateWithPercentage(num1, num2);

        // Assert (Verificación)
        StepVerifier.create(resultadoMono)
            .assertNext(resultado -> {
                assertThat(resultado.sum()).isEqualByComparingTo(new BigDecimal("10"));
                assertThat(resultado.result()).isEqualByComparingTo(new BigDecimal("15"));
            })
            .verifyComplete();
    }
}
