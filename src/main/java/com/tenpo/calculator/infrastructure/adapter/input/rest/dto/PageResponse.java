package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(description = "Respuesta paginada")
public record PageResponse<T>(
    @Schema(description = "Contenido de la página actual")
    List<T> content,

    @Schema(description = "Número de página actual (comienza en 0)", example = "0")
    int page,

    @Schema(description = "Cantidad de elementos por página", example = "10")
    int size,

    @Schema(description = "Número total de elementos", example = "25")
    long totalElements,

    @Schema(description = "Número total de páginas", example = "3")
    int totalPages,

    @Schema(description = "Indica si es la primera página", example = "true")
    boolean first,

    @Schema(description = "Indica si es la última página", example = "false")
    boolean last
) {
    public static <T, R> PageResponse<R> fromPage(Page<T> pagina, Function<T, R> transformador) {
        return new PageResponse<>(
            pagina.getContent().stream().map(transformador).toList(),
            pagina.getNumber(),
            pagina.getSize(),
            pagina.getTotalElements(),
            pagina.getTotalPages(),
            pagina.isFirst(),
            pagina.isLast()
        );
    }
}
