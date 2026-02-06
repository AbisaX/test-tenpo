package com.tenpo.calculator.infrastructure.filter;

import com.tenpo.calculator.domain.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter implements WebFilter {

    private final Bucket bucketPeticiones;

    private static final String RUTA_API_BASE = "/api/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String ruta = exchange.getRequest().getPath().value();

        // El rate limit solo aplica a los endpoints de la API pública.
        if (!ruta.startsWith(RUTA_API_BASE)) {
            return chain.filter(exchange);
        }

        if (bucketPeticiones.tryConsume(1)) {
            log.debug("Rate limit OK para {}", ruta);
            return chain.filter(exchange);
        }

        log.warn("Rate limit excedido para {}", ruta);
        return Mono.error(new RateLimitExceededException(
            "Límite de peticiones excedido. Máximo 3 por minuto. Intenta nuevamente más tarde."
        ));
    }
}
