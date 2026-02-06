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

        log.warn("Límite de peticiones excedido: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Demasiadas Peticiones",
            "Límite de peticiones excedido. Máximo 3 peticiones por minuto permitidas. Por favor, intente nuevamente más tarde.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(errorResponse));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleExternalServiceException(
            ExternalServiceException ex,
            ServerWebExchange exchange) {

        log.error("Error del servicio externo: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Servicio No Disponible",
            "El servicio externo de porcentaje no está disponible después de 3 intentos. Por favor, intente nuevamente más tarde.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        log.warn("Error de validación: {}", ex.getMessage());

        String errorMessage = ex.getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Error de Validación",
            errorMessage,
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInputException(
            ServerWebInputException ex,
            ServerWebExchange exchange) {

        log.warn("Error de entrada: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Solicitud Incorrecta",
            "El cuerpo de la solicitud es inválido o tiene un formato incorrecto. Verifique los datos enviados.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {

        log.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Solicitud Incorrecta",
            "Los parámetros proporcionados son inválidos: " + ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {

        log.error("Error inesperado: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error Interno del Servidor",
            "Ocurrió un error inesperado. Por favor, intente nuevamente más tarde.",
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse));
    }
}
