package com.tenpo.calculator.infrastructure.exception;

import com.tenpo.calculator.domain.exception.ExternalServiceException;
import com.tenpo.calculator.domain.exception.RateLimitExceededException;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRateLimitExceeded(
            RateLimitExceededException ex,
            ServerWebExchange exchange) {

        log.warn("Rate limit excedido: {}", ex.getMessage());

        ErrorResponse respuesta = ErrorResponse.of(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Demasiadas peticiones",
            "Límite de peticiones excedido. Máximo 3 por minuto. Intenta más tarde.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(respuesta));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleExternalServiceException(
            ExternalServiceException ex,
            ServerWebExchange exchange) {

        log.error("Error del servicio externo: {}", ex.getMessage());

        ErrorResponse respuesta = ErrorResponse.of(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Servicio no disponible",
            "El servicio de porcentaje no respondió después de 3 intentos.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(respuesta));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        log.warn("Error de validación: {}", ex.getMessage());

        String detalle = ex.getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ErrorResponse respuesta = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Error de validación",
            detalle,
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(respuesta));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInputException(
            ServerWebInputException ex,
            ServerWebExchange exchange) {

        log.warn("Error de entrada: {}", ex.getMessage());

        ErrorResponse respuesta = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Solicitud inválida",
            "El cuerpo de la solicitud no es válido o tiene un formato incorrecto.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(respuesta));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {

        log.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse respuesta = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Solicitud inválida",
            "Parámetros inválidos: " + ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(respuesta));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {

        log.error("Error inesperado: {}", ex.getMessage(), ex);

        ErrorResponse respuesta = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno del servidor",
            "Ocurrió un error inesperado. Intenta nuevamente más tarde.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(respuesta));
    }
}
