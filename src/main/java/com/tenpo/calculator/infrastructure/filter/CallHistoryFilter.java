package com.tenpo.calculator.infrastructure.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.calculator.domain.model.CallHistory;
import com.tenpo.calculator.domain.port.input.CallHistoryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
@Slf4j
public class CallHistoryFilter implements WebFilter {

    private final CallHistoryUseCase callHistoryUseCase;
    private final ObjectMapper objectMapper;

    private static final String API_PATH_PREFIX = "/api/v1/";
    private static final String HISTORY_PATH = "/api/v1/history";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Only log API calls (excluding the history endpoint itself to avoid recursion)
        if (!path.startsWith(API_PATH_PREFIX) || path.startsWith(HISTORY_PATH)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        ResponseBodyCapture responseCapture = new ResponseBodyCapture();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux<? extends DataBuffer> fluxBody) {
                    return super.writeWith(fluxBody.doOnNext(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);
                        responseCapture.appendContent(new String(content, StandardCharsets.UTF_8));
                    }).map(dataBuffer -> exchange.getResponse().bufferFactory().wrap(
                        responseCapture.getContent().getBytes(StandardCharsets.UTF_8)
                    )));
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build())
            .doFinally(signalType -> {
                try {
                    saveCallHistory(request, exchange.getResponse(), responseCapture.getContent());
                } catch (Exception e) {
                    log.error("Error saving call history: {}", e.getMessage());
                }
            });
    }

    private void saveCallHistory(ServerHttpRequest request, ServerHttpResponse response, String responseBody) {
        String endpoint = request.getPath().value();
        String httpMethod = request.getMethod().name();
        String parameters = extractParameters(request);
        Integer statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : null;
        boolean success = statusCode != null && statusCode >= 200 && statusCode < 400;

        String truncatedResponse = responseBody;
        if (truncatedResponse != null && truncatedResponse.length() > 2000) {
            truncatedResponse = truncatedResponse.substring(0, 2000) + "...[truncated]";
        }

        CallHistory callHistory = CallHistory.create(
            endpoint,
            httpMethod,
            parameters,
            truncatedResponse,
            statusCode,
            success
        );

        // Save asynchronously
        callHistoryUseCase.saveCallHistoryAsync(callHistory);
    }

    private String extractParameters(ServerHttpRequest request) {
        Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();

        try {
            if (!queryParams.isEmpty()) {
                return objectMapper.writeValueAsString(queryParams);
            }
            return "{}";
        } catch (JsonProcessingException e) {
            log.warn("Error serializing parameters: {}", e.getMessage());
            return queryParams.toString();
        }
    }

    private static class ResponseBodyCapture {
        private final StringBuilder content = new StringBuilder();

        void appendContent(String chunk) {
            content.append(chunk);
        }

        String getContent() {
            return content.toString();
        }
    }
}
