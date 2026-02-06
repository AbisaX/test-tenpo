package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResponse - Tests unitarios")
class PageResponseTest {

    @Test
    @DisplayName("Debe crear PageResponse desde Spring Page")
    void debeCrearDesdeSpringPage() {
        List<String> contenido = List.of("item1", "item2", "item3");
        PageImpl<String> pagina = new PageImpl<>(contenido, PageRequest.of(0, 10), 3);

        PageResponse<String> respuesta = PageResponse.fromPage(pagina, s -> s);

        assertThat(respuesta.content()).hasSize(3);
        assertThat(respuesta.page()).isZero();
        assertThat(respuesta.size()).isEqualTo(10);
        assertThat(respuesta.totalElements()).isEqualTo(3);
        assertThat(respuesta.totalPages()).isEqualTo(1);
        assertThat(respuesta.first()).isTrue();
        assertThat(respuesta.last()).isTrue();
    }

    @Test
    @DisplayName("Debe aplicar función de transformación al contenido")
    void debeAplicarFuncionDeTransformacion() {
        List<Integer> contenido = List.of(1, 2, 3);
        PageImpl<Integer> pagina = new PageImpl<>(contenido, PageRequest.of(0, 10), 3);

        PageResponse<String> respuesta = PageResponse.fromPage(pagina, num -> "numero-" + num);

        assertThat(respuesta.content()).containsExactly("numero-1", "numero-2", "numero-3");
    }

    @Test
    @DisplayName("Debe manejar página intermedia correctamente")
    void debeManejarPaginaIntermedia() {
        List<String> contenido = List.of("item1", "item2");
        PageImpl<String> pagina = new PageImpl<>(contenido, PageRequest.of(1, 2), 6);

        PageResponse<String> respuesta = PageResponse.fromPage(pagina, s -> s);

        assertThat(respuesta.page()).isEqualTo(1);
        assertThat(respuesta.size()).isEqualTo(2);
        assertThat(respuesta.totalElements()).isEqualTo(6);
        assertThat(respuesta.totalPages()).isEqualTo(3);
        assertThat(respuesta.first()).isFalse();
        assertThat(respuesta.last()).isFalse();
    }

    @Test
    @DisplayName("Debe manejar página vacía")
    void debeManejarPaginaVacia() {
        List<String> contenido = List.of();
        PageImpl<String> pagina = new PageImpl<>(contenido, PageRequest.of(0, 10), 0);

        PageResponse<String> respuesta = PageResponse.fromPage(pagina, s -> s);

        assertThat(respuesta.content()).isEmpty();
        assertThat(respuesta.totalElements()).isZero();
        assertThat(respuesta.totalPages()).isZero();
    }
}
