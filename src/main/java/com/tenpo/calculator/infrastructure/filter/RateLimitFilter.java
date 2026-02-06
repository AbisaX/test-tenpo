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

    private final Bucket bucket;

    private static final String API_PATH_PREFIX = "/api/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Solo aplicar rate limiting a endpoints de la API
        if (!path.startsWith(API_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        if (bucket.tryConsume(1)) {
            log.debug("Verificación de límite de peticiones exitosa para: {}", path);
            return chain.filter(exchange);
        }

        log.warn("Límite de peticiones excedido para: {}", path);
        return Mono.error(new RateLimitExceededException(
            "Límite de peticiones excedido. Máximo 3 peticiones por minuto permitidas. Por favor, intente nuevamente más tarde."
        ));
    }
}
