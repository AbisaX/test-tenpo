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

    private final CallHistoryUseCase casoUsoHistorial;
    private final ObjectMapper mapeadorJson;

    private static final String PREFIJO_RUTA_API = "/api/v1/";
    private static final String RUTA_HISTORIAL = "/api/v1/history";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String ruta = exchange.getRequest().getPath().value();

        // Solo registrar llamadas a la API (excluyendo el endpoint de historial para evitar recursión)
        if (!ruta.startsWith(PREFIJO_RUTA_API) || ruta.startsWith(RUTA_HISTORIAL)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest peticion = exchange.getRequest();
        CapturaRespuesta capturaRespuesta = new CapturaRespuesta();

        ServerHttpResponseDecorator respuestaDecorada = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux<? extends DataBuffer> fluxBody) {
                    return super.writeWith(fluxBody.doOnNext(dataBuffer -> {
                        byte[] contenido = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(contenido);
                        DataBufferUtils.release(dataBuffer);
                        capturaRespuesta.agregarContenido(new String(contenido, StandardCharsets.UTF_8));
                    }).map(dataBuffer -> exchange.getResponse().bufferFactory().wrap(
                        capturaRespuesta.obtenerContenido().getBytes(StandardCharsets.UTF_8)
                    )));
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(respuestaDecorada).build())
            .doFinally(tipoSenal -> {
                try {
                    guardarHistorial(peticion, exchange.getResponse(), capturaRespuesta.obtenerContenido());
                } catch (Exception e) {
                    log.error("Error al guardar historial de llamadas: {}", e.getMessage());
                }
            });
    }

    private void guardarHistorial(ServerHttpRequest peticion, ServerHttpResponse respuesta, String cuerpoRespuesta) {
        String endpoint = peticion.getPath().value();
        String metodoHttp = peticion.getMethod().name();
        String parametros = extraerParametros(peticion);
        Integer codigoEstado = respuesta.getStatusCode() != null ? respuesta.getStatusCode().value() : null;
        boolean exitoso = codigoEstado != null && codigoEstado >= 200 && codigoEstado < 400;

        String respuestaTruncada = cuerpoRespuesta;
        if (respuestaTruncada != null && respuestaTruncada.length() > 2000) {
            respuestaTruncada = respuestaTruncada.substring(0, 2000) + "...[truncado]";
        }

        CallHistory historialLlamada = CallHistory.create(
            endpoint,
            metodoHttp,
            parametros,
            respuestaTruncada,
            codigoEstado,
            exitoso
        );

        // Guardar de forma asíncrona
        casoUsoHistorial.saveCallHistoryAsync(historialLlamada);
    }

    private String extraerParametros(ServerHttpRequest peticion) {
        Map<String, String> parametrosConsulta = peticion.getQueryParams().toSingleValueMap();

        try {
            if (!parametrosConsulta.isEmpty()) {
                return mapeadorJson.writeValueAsString(parametrosConsulta);
            }
            return "{}";
        } catch (JsonProcessingException e) {
            log.warn("Error al serializar parámetros: {}", e.getMessage());
            return parametrosConsulta.toString();
        }
    }

    private static class CapturaRespuesta {
        private final StringBuilder contenido = new StringBuilder();

        void agregarContenido(String fragmento) {
            contenido.append(fragmento);
        }

        String obtenerContenido() {
            return contenido.toString();
        }
    }
}
