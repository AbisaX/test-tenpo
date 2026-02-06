package com.tenpo.calculator.infrastructure.adapter.output.external;

import com.tenpo.calculator.domain.exception.ExternalServiceException;
import com.tenpo.calculator.domain.port.output.PercentageServicePort;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@Slf4j
public class PercentageServiceAdapter implements PercentageServicePort {

    private final WebClient webClient;
    private final Retry retry;
    private final String percentageEndpoint;

    public PercentageServiceAdapter(
            WebClient.Builder webClientBuilder,
            RetryRegistry retryRegistry,
            @Value("${external-service.url}") String externalServiceUrl,
            @Value("${external-service.percentage-endpoint}") String percentageEndpoint) {

        this.webClient = webClientBuilder
            .baseUrl(externalServiceUrl)
            .build();
        this.retry = retryRegistry.retry("externalService");
        this.percentageEndpoint = percentageEndpoint;

        log.info("PercentageServiceAdapter initialized with URL: {}{}", externalServiceUrl, percentageEndpoint);
    }

    @Override
    public Mono<BigDecimal> getPercentage() {
        log.debug("Fetching percentage from external service");

        return webClient.get()
            .uri(percentageEndpoint)
            .retrieve()
            .bodyToMono(PercentageResponse.class)
            .map(PercentageResponse::percentage)
            .timeout(Duration.ofSeconds(5))
            .transformDeferred(RetryOperator.of(retry))
            .doOnError(e -> log.error("Error fetching percentage after retries: {}", e.getMessage()))
            .onErrorMap(this::mapToExternalServiceException);
    }

    private Throwable mapToExternalServiceException(Throwable throwable) {
        if (throwable instanceof ExternalServiceException) {
            return throwable;
        }
        if (throwable instanceof WebClientResponseException wcre) {
            return new ExternalServiceException(
                "External service returned error: " + wcre.getStatusCode() + " - " + wcre.getMessage(),
                wcre
            );
        }
        return new ExternalServiceException(
            "External service unavailable after 3 retry attempts: " + throwable.getMessage(),
            throwable
        );
    }

    private record PercentageResponse(BigDecimal percentage) {}
}
