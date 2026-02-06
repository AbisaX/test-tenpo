package com.tenpo.calculator.infrastructure.filter;

import com.tenpo.calculator.domain.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter - Tests unitarios")
class RateLimitFilterTest {

    @Mock
    private WebFilterChain filterChain;

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void configurar() {
        Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(3)
                .refillGreedy(3, Duration.ofMinutes(1))
                .build())
            .build();
        rateLimitFilter = new RateLimitFilter(bucket);
    }

    @Test
    @DisplayName("Debe permitir la petición cuando no se excede el límite")
    void debePermitirPeticionCuandoNoExcedeLimite() {
        MockServerHttpRequest request = MockServerHttpRequest
            .method(HttpMethod.POST, "/api/v1/calculator/calculate")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(rateLimitFilter.filter(exchange, filterChain))
            .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Debe bloquear la petición cuando se excede el límite")
    void debeBloquearPeticionCuandoExcedeLimite() {
        Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(1)
                .refillGreedy(1, Duration.ofMinutes(1))
                .build())
            .build();
        rateLimitFilter = new RateLimitFilter(bucket);

        MockServerHttpRequest peticion1 = MockServerHttpRequest
            .method(HttpMethod.POST, "/api/v1/calculator/calculate")
            .build();
        MockServerWebExchange exchange1 = MockServerWebExchange.from(peticion1);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(rateLimitFilter.filter(exchange1, filterChain))
            .verifyComplete();

        MockServerHttpRequest peticion2 = MockServerHttpRequest
            .method(HttpMethod.POST, "/api/v1/calculator/calculate")
            .build();
        MockServerWebExchange exchange2 = MockServerWebExchange.from(peticion2);

        StepVerifier.create(rateLimitFilter.filter(exchange2, filterChain))
            .expectError(RateLimitExceededException.class)
            .verify();
    }

    @Test
    @DisplayName("No debe aplicar límite de peticiones a rutas fuera de la API")
    void noDebeAplicarLimiteARutasFueraDeApi() {
        MockServerHttpRequest request = MockServerHttpRequest
            .method(HttpMethod.GET, "/swagger-ui.html")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(rateLimitFilter.filter(exchange, filterChain))
            .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("No debe aplicar límite de peticiones a endpoints de actuator")
    void noDebeAplicarLimiteAEndpointsDeActuator() {
        MockServerHttpRequest request = MockServerHttpRequest
            .method(HttpMethod.GET, "/actuator/health")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(rateLimitFilter.filter(exchange, filterChain))
            .verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    @DisplayName("Debe permitir exactamente 3 peticiones por minuto")
    void debePermitirExactamenteTresPeticionesPorMinuto() {
        Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(3)
                .refillGreedy(3, Duration.ofMinutes(1))
                .build())
            .build();
        rateLimitFilter = new RateLimitFilter(bucket);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        for (int i = 0; i < 3; i++) {
            MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/v1/calculator/calculate")
                .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(rateLimitFilter.filter(exchange, filterChain))
                .verifyComplete();
        }

        MockServerHttpRequest peticion4 = MockServerHttpRequest
            .method(HttpMethod.POST, "/api/v1/calculator/calculate")
            .build();
        MockServerWebExchange exchange4 = MockServerWebExchange.from(peticion4);

        StepVerifier.create(rateLimitFilter.filter(exchange4, filterChain))
            .expectError(RateLimitExceededException.class)
            .verify();
    }
}
