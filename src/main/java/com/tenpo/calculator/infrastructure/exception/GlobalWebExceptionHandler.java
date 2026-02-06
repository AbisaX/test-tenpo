package com.tenpo.calculator.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.calculator.domain.exception.ExternalServiceException;
import com.tenpo.calculator.domain.exception.RateLimitExceededException;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper jsonMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String titulo;
        String detalle;

        if (ex instanceof RateLimitExceededException) {
            status = HttpStatus.TOO_MANY_REQUESTS;
            titulo = "Demasiadas peticiones";
            detalle = "Límite de peticiones excedido. Máximo 3 por minuto. Intenta más tarde.";
            log.warn("Rate limit excedido: {}", ex.getMessage());
        } else if (ex instanceof ExternalServiceException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            titulo = "Servicio no disponible";
            detalle = "El servicio de porcentaje no respondió después de 3 intentos.";
            log.error("Error del servicio externo: {}", ex.getMessage());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            titulo = "Error interno del servidor";
            detalle = "Ocurrió un error inesperado. Intenta nuevamente más tarde.";
            log.error("Error inesperado: {}", ex.getMessage(), ex);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse respuesta = ErrorResponse.of(
            status.value(),
            titulo,
            detalle,
            exchange.getRequest().getPath().value()
        );

        try {
            byte[] bytes = jsonMapper.writeValueAsBytes(respuesta);
            return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error al serializar respuesta de error", e);
            return exchange.getResponse().setComplete();
        }
    }
}
