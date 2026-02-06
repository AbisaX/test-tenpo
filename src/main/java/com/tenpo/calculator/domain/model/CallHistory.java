package com.tenpo.calculator.domain.model;

import java.time.LocalDateTime;

public record CallHistory(
    Long id,
    LocalDateTime timestamp,
    String endpoint,
    String httpMethod,
    String parameters,
    String response,
    Integer statusCode,
    boolean success
) {
    public static CallHistory create(
            String endpoint,
            String httpMethod,
            String parameters,
            String response,
            Integer statusCode,
            boolean success) {
        return new CallHistory(
            null,
            LocalDateTime.now(),
            endpoint,
            httpMethod,
            parameters,
            response,
            statusCode,
            success
        );
    }
}
