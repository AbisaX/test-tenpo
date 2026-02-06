package com.tenpo.calculator.infrastructure.adapter.input.rest;

import com.tenpo.calculator.domain.model.CallHistory;
import com.tenpo.calculator.domain.port.input.CallHistoryUseCase;
import com.tenpo.calculator.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CallHistoryController - Tests Unitarios")
class CallHistoryControllerTest {

    @Mock
    private CallHistoryUseCase callHistoryUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void configurar() {
        CallHistoryController controller = new CallHistoryController(callHistoryUseCase);
        webTestClient = WebTestClient.bindToController(controller)
            .controllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("Debe retornar historial de llamadas paginado con estado 200 OK")
    void debeRetornarHistorialPaginado() {
        // Arrange (Preparación)
        List<CallHistory> listaHistorial = List.of(
            new CallHistory(1L, LocalDateTime.now(), "/api/v1/calculator/calculate", "POST",
                "{\"num1\": 5}", "{\"result\": 11}", 200, true),
            new CallHistory(2L, LocalDateTime.now(), "/api/v1/calculator/calculate", "POST",
                "{\"num1\": 10}", "{\"result\": 22}", 200, true)
        );

        when(callHistoryUseCase.getCallHistory(0, 10))
            .thenReturn(Mono.just(new PageImpl<>(listaHistorial, PageRequest.of(0, 10), 2)));

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/api/v1/history?page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content").isArray()
            .jsonPath("$.content.length()").isEqualTo(2)
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(10)
            .jsonPath("$.totalElements").isEqualTo(2)
            .jsonPath("$.content[0].endpoint").isEqualTo("/api/v1/calculator/calculate")
            .jsonPath("$.content[0].httpMethod").isEqualTo("POST")
            .jsonPath("$.content[0].success").isEqualTo(true);

        verify(callHistoryUseCase).getCallHistory(0, 10);
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no existe historial")
    void debeRetornarPaginaVacia() {
        // Arrange (Preparación)
        when(callHistoryUseCase.getCallHistory(0, 10))
            .thenReturn(Mono.just(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)));

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/api/v1/history?page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content").isArray()
            .jsonPath("$.content.length()").isEqualTo(0)
            .jsonPath("$.totalElements").isEqualTo(0);
    }

    @Test
    @DisplayName("Debe usar valores de paginación por defecto")
    void debeUsarValoresPorDefecto() {
        // Arrange (Preparación)
        when(callHistoryUseCase.getCallHistory(0, 10))
            .thenReturn(Mono.just(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)));

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/api/v1/history")
            .exchange()
            .expectStatus().isOk();

        verify(callHistoryUseCase).getCallHistory(0, 10);
    }

    @Test
    @DisplayName("Debe manejar diferentes tamaños de página")
    void debeManejarDiferentesTamaniosDePagina() {
        // Arrange (Preparación)
        when(callHistoryUseCase.getCallHistory(1, 5))
            .thenReturn(Mono.just(new PageImpl<>(List.of(), PageRequest.of(1, 5), 0)));

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/api/v1/history?page=1&size=5")
            .exchange()
            .expectStatus().isOk();

        verify(callHistoryUseCase).getCallHistory(1, 5);
    }

    @Test
    @DisplayName("Debe incluir entradas con errores en el historial")
    void debeIncluirEntradasConErrores() {
        // Arrange (Preparación)
        List<CallHistory> listaHistorial = List.of(
            new CallHistory(1L, LocalDateTime.now(), "/api/v1/calculator/calculate", "POST",
                "{}", "{\"error\": \"Solicitud Incorrecta\"}", 400, false)
        );

        when(callHistoryUseCase.getCallHistory(0, 10))
            .thenReturn(Mono.just(new PageImpl<>(listaHistorial, PageRequest.of(0, 10), 1)));

        // Act & Assert (Acción y Verificación)
        webTestClient.get()
            .uri("/api/v1/history?page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content[0].success").isEqualTo(false)
            .jsonPath("$.content[0].statusCode").isEqualTo(400);
    }
}
