package com.tenpo.calculator.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-minute:3}")
    private int maxPeticionesPorMinuto;

    @Bean
    public Bucket rateLimitBucket() {
        Bandwidth limitePorMinuto = Bandwidth.builder()
            .capacity(maxPeticionesPorMinuto)
            .refillGreedy(maxPeticionesPorMinuto, Duration.ofMinutes(1))
            .build();

        return Bucket.builder()
            .addLimit(limitePorMinuto)
            .build();
    }
}
