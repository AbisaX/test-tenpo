package com.tenpo.calculator.infrastructure.adapter.input.rest;

import com.tenpo.calculator.domain.port.input.CallHistoryUseCase;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.CallHistoryResponse;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.ErrorResponse;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Historial de Llamadas", description = "Consulta del historial de llamadas a la API")
public class CallHistoryController {

    private final CallHistoryUseCase callHistoryUseCase;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Obtener historial de llamadas",
        description = "Recupera el historial paginado de todas las llamadas realizadas a los endpoints de la API. " +
                      "Incluye fecha/hora, endpoint, parámetros, respuesta y código de estado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Historial recuperado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de paginación inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Límite de peticiones excedido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Mono<PageResponse<CallHistoryResponse>> getHistory(
            @Parameter(description = "Número de página (comienza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Cantidad de elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("Consultando historial de llamadas: pagina={}, tamaño={}", page, size);

        return callHistoryUseCase.getCallHistory(page, size)
            .map(pageResult -> PageResponse.fromPage(pageResult, CallHistoryResponse::fromDomain))
            .doOnSuccess(response -> log.info("Se recuperaron {} registros del historial", response.content().size()));
    }
}
