package com.tenpo.calculator.domain.port.input;

import com.tenpo.calculator.domain.model.CalculationResult;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CalculatorUseCase {

    Mono<CalculationResult> calculateWithPercentage(BigDecimal num1, BigDecimal num2);
}
