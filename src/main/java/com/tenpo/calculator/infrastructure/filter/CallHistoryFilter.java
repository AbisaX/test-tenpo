package com.tenpo.calculator.infrastructure.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.calculator.domain.model.CallHistory;
import com.tenpo.calculator.domain.port.input.CallHistoryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * Filtro que intercepta las llamadas a la API de cálculo y registra
 * la petición y su respuesta en el historial. Se ejecuta después del
 * RateLimitFilter para no registrar peticiones que fueron rechazadas.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
@Slf4j
public class CallHistoryFilter implements WebFilter {

    private final CallHistoryUseCase servicioHistorial;
    private final ObjectMapper jsonMapper;

    @Value("${call-history.max-response-length:2000}")
    private int maxLargoRespuesta;

    private static final String RUTA_API = "/api/v1/";
    private static final String RUTA_HISTORIAL = "/api/v1/history";
    private static final String SUFIJO_TRUNCADO = "...(recortado)";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String ruta = exchange.getRequest().getPath().value();

        // Se excluye el endpoint de historial para que consultar el historial
        // no genere nuevos registros y el almacenamiento crezca sin control.
        if (!ruta.startsWith(RUTA_API) || ruta.startsWith(RUTA_HISTORIAL)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        AcumuladorRespuesta acumuladorRespuesta = new AcumuladorRespuesta();

        // Se decora la respuesta para capturar el cuerpo antes de enviarlo al cliente.
        // En WebFlux el cuerpo es un stream de una sola lectura.
        ServerHttpResponseDecorator respuestaDecorada = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> cuerpo) {
                if (cuerpo instanceof Flux<? extends DataBuffer> flujoCuerpo) {
                    return super.writeWith(flujoCuerpo.doOnNext(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);
                        acumuladorRespuesta.append(new String(content, StandardCharsets.UTF_8));
                    }).map(dataBuffer -> exchange.getResponse().bufferFactory().wrap(
                        acumuladorRespuesta.getContent().getBytes(StandardCharsets.UTF_8)
                    )));
                }
                return super.writeWith(cuerpo);
            }
        };

        return chain.filter(exchange.mutate().response(respuestaDecorada).build())
            .doFinally(signalType -> {
                try {
                    registrarLlamada(request, exchange.getResponse(), acumuladorRespuesta.getContent());
                } catch (Exception e) {
                    log.error("No se pudo registrar llamada en historial para {}: {}",
                        request.getPath().value(), e.getMessage());
                }
            });
    }

    private void registrarLlamada(ServerHttpRequest request, ServerHttpResponse response, String cuerpoRespuesta) {
        String endpoint = request.getPath().value();
        String httpMethod = request.getMethod().name();
        String parametros = serializarQueryParams(request);
        Integer codigoEstado = response.getStatusCode() != null ? response.getStatusCode().value() : null;
        boolean exitoso = codigoEstado != null && codigoEstado >= 200 && codigoEstado < 400;

        // Se recorta para evitar respuestas enormes en call_history y mantener consultas ágiles.
        // El límite se configura en application.yml.
        String cuerpoRecortado = cuerpoRespuesta;
        if (cuerpoRecortado != null && cuerpoRecortado.length() > maxLargoRespuesta) {
            cuerpoRecortado = cuerpoRecortado.substring(0, maxLargoRespuesta) + SUFIJO_TRUNCADO;
        }

        CallHistory record = CallHistory.create(
            endpoint, httpMethod, parametros, cuerpoRecortado, codigoEstado, exitoso
        );

        servicioHistorial.saveCallHistoryAsync(record);
    }

    private String serializarQueryParams(ServerHttpRequest request) {
        Map<String, String> parametros = request.getQueryParams().toSingleValueMap();
        if (parametros.isEmpty()) {
            return "{}";
        }
        try {
            return jsonMapper.writeValueAsString(parametros);
        } catch (JsonProcessingException e) {
            log.warn("No se pudieron serializar los parámetros de {}: {}",
                request.getPath().value(), e.getMessage());
            return parametros.toString();
        }
    }

    private static class AcumuladorRespuesta {
        private final StringBuilder buffer = new StringBuilder();

        void append(String chunk) {
            buffer.append(chunk);
        }

        String getContent() {
            return buffer.toString();
        }
    }
}
