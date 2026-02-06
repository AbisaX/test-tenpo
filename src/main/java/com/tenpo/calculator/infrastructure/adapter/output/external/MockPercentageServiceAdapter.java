package com.tenpo.calculator.infrastructure.adapter.output.external;

import com.tenpo.calculator.domain.port.output.PercentageServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@Primary
@ConditionalOnProperty(name = "external-service.mock.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MockPercentageServiceAdapter implements PercentageServicePort {

    private static final BigDecimal PORCENTAJE_MOCK = BigDecimal.valueOf(10);

    @Override
    public Mono<BigDecimal> getPercentage() {
        log.debug("Retornando porcentaje mock: {}%", PORCENTAJE_MOCK);
        return Mono.just(PORCENTAJE_MOCK)
            .delayElement(Duration.ofMillis(100)); // Simulando latencia de red
    }
}
