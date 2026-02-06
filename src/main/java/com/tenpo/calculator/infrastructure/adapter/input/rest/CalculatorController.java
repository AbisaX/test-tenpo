package com.tenpo.calculator.infrastructure.adapter.input.rest;

import com.tenpo.calculator.domain.port.input.CalculatorUseCase;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.CalculationRequest;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.CalculationResponse;
import com.tenpo.calculator.infrastructure.adapter.input.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/calculator")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Calculadora", description = "Operaciones de cálculo con porcentaje dinámico")
public class CalculatorController {

    private final CalculatorUseCase calculatorUseCase;

    @PostMapping(value = "/calculate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Calcular suma con porcentaje",
        description = "Recibe dos números, los suma y aplica un porcentaje obtenido de un servicio externo. " +
                      "Por ejemplo: si num1=5, num2=5 y el porcentaje es 10%, el resultado será (5+5) + 10% = 11"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cálculo realizado exitosamente",
            content = @Content(schema = @Schema(implementation = CalculationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Límite de peticiones excedido (máximo 3 por minuto)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Servicio externo no disponible después de 3 reintentos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Mono<CalculationResponse> calculate(@Valid @RequestBody CalculationRequest request) {
        log.info("Received calculation request: num1={}, num2={}", request.num1(), request.num2());

        return calculatorUseCase.calculateWithPercentage(request.num1(), request.num2())
            .map(CalculationResponse::fromDomain)
            .doOnSuccess(response -> log.info("Calculation completed successfully: {}", response));
    }
}
