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

    private final Bucket contenedorTokens;

    private static final String PREFIJO_RUTA_API = "/api/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String ruta = exchange.getRequest().getPath().value();

        // Solo aplicar rate limiting a endpoints de la API
        if (!ruta.startsWith(PREFIJO_RUTA_API)) {
            return chain.filter(exchange);
        }

        if (contenedorTokens.tryConsume(1)) {
            log.debug("Verificación de límite de peticiones exitosa para: {}", ruta);
            return chain.filter(exchange);
        }

        log.warn("Límite de peticiones excedido para: {}", ruta);
        return Mono.error(new RateLimitExceededException(
            "Límite de peticiones excedido. Máximo 3 peticiones por minuto permitidas. Por favor, intente nuevamente más tarde."
        ));
    }
}
