package com.tenpo.calculator.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResponse - Tests Unitarios")
class PageResponseTest {

    @Test
    @DisplayName("Debe crear PageResponse desde Spring Page")
    void debeCrearDesdeSpringPage() {
        // Arrange (Preparación)
        List<String> contenido = List.of("item1", "item2", "item3");
        PageImpl<String> pagina = new PageImpl<>(contenido, PageRequest.of(0, 10), 3);

        // Act (Acción)
        PageResponse<String> respuesta = PageResponse.fromPage(pagina, s -> s);

        // Assert (Verificación)
        assertThat(respuesta.content()).hasSize(3);
        assertThat(respuesta.page()).isZero();
        assertThat(respuesta.size()).isEqualTo(10);
        assertThat(respuesta.totalElements()).isEqualTo(3);
        assertThat(respuesta.totalPages()).isEqualTo(1);
        assertThat(respuesta.first()).isTrue();
        assertThat(respuesta.last()).isTrue();
    }

    @Test
    @DisplayName("Debe aplicar función de mapeo al contenido")
    void debeAplicarFuncionDeMapeo() {
        // Arrange (Preparación)
        List<Integer> contenido = List.of(1, 2, 3);
        PageImpl<Integer> pagina = new PageImpl<>(contenido, PageRequest.of(0, 10), 3);

        // Act (Acción)
        PageResponse<String> respuesta = PageResponse.fromPage(pagina, num -> "numero-" + num);

        // Assert (Verificación)
        assertThat(respuesta.content()).containsExactly("numero-1", "numero-2", "numero-3");
    }

    @Test
    @DisplayName("Debe manejar página intermedia correctamente")
    void debeManejarPaginaIntermedia() {
        // Arrange (Preparación)
        List<String> contenido = List.of("item1", "item2");
        PageImpl<String> pagina = new PageImpl<>(contenido, PageRequest.of(1, 2), 6);

        // Act (Acción)
        PageResponse<String> respuesta = PageResponse.fromPage(pagina, s -> s);

        // Assert (Verificación)
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
        // Arrange (Preparación)
        List<String> contenido = List.of();
        PageImpl<String> pagina = new PageImpl<>(contenido, PageRequest.of(0, 10), 0);

        // Act (Acción)
        PageResponse<String> respuesta = PageResponse.fromPage(pagina, s -> s);

        // Assert (Verificación)
        assertThat(respuesta.content()).isEmpty();
        assertThat(respuesta.totalElements()).isZero();
        assertThat(respuesta.totalPages()).isZero();
    }
}
