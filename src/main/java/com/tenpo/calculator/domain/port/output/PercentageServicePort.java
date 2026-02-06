package com.tenpo.calculator.domain.port.output;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PercentageServicePort {

    Mono<BigDecimal> getPercentage();
}
