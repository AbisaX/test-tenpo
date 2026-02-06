package com.tenpo.calculator.application.service;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.input.CalculatorUseCase;
import com.tenpo.calculator.domain.port.output.PercentageServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculatorService implements CalculatorUseCase {

    private final PercentageServicePort servicioPorcentaje;

    @Override
    public Mono<CalculationResult> calculateWithPercentage(BigDecimal num1, BigDecimal num2) {
        log.debug("Calculando con porcentaje para num1={}, num2={}", num1, num2);

        return servicioPorcentaje.getPercentage()
            .map(porcentaje -> {
                log.debug("Porcentaje recibido: {}%", porcentaje);
                return CalculationResult.of(num1, num2, porcentaje);
            })
            .doOnSuccess(resultado -> log.debug("Resultado del cálculo: {}", resultado));
    }
}
