package com.tenpo.calculator.application.service;

import com.tenpo.calculator.domain.model.CallHistory;
import com.tenpo.calculator.domain.port.output.CallHistoryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CallHistoryService - Tests unitarios")
class CallHistoryServiceTest {

    @Mock
    private CallHistoryRepositoryPort repositorioHistorial;

    private CallHistoryService callHistoryService;

    @BeforeEach
    void configurar() {
        callHistoryService = new CallHistoryService(repositorioHistorial);
    }

    @Test
    @DisplayName("Debe guardar el historial de llamadas de forma asíncrona")
    void debeGuardarHistorialDeFormaAsincrona() throws InterruptedException {
        CallHistory historial = CallHistory.create(
            "/api/v1/calculator/calculate",
            "POST",
            "{\"num1\": 5, \"num2\": 5}",
            "{\"result\": 11}",
            200,
            true
        );

        CallHistory historialGuardado = new CallHistory(
            1L,
            historial.timestamp(),
            historial.endpoint(),
            historial.httpMethod(),
            historial.parameters(),
            historial.response(),
            historial.statusCode(),
            historial.success()
        );

        when(repositorioHistorial.save(any(CallHistory.class)))
            .thenReturn(Mono.just(historialGuardado));

        callHistoryService.saveCallHistoryAsync(historial);

        Thread.sleep(500);
        verify(repositorioHistorial, times(1)).save(any(CallHistory.class));
    }

    @Test
    @DisplayName("Debe obtener el historial de llamadas paginado")
    void debeObtenerHistorialPaginado() {
        int pagina = 0;
        int tamanio = 10;

        List<CallHistory> listaHistorial = List.of(
            new CallHistory(1L, LocalDateTime.now(), "/api/v1/calculator/calculate", "POST", "{}", "{}", 200, true),
            new CallHistory(2L, LocalDateTime.now(), "/api/v1/history", "GET", "{}", "[]", 200, true)
        );

        when(repositorioHistorial.findAllPaginated(0, 10))
            .thenReturn(Flux.fromIterable(listaHistorial));
        when(repositorioHistorial.count())
            .thenReturn(Mono.just(2L));

        Mono<Page<CallHistory>> resultadoMono = callHistoryService.getCallHistory(pagina, tamanio);

        StepVerifier.create(resultadoMono)
            .assertNext(paginaResultado -> {
                assertThat(paginaResultado.getContent()).hasSize(2);
                assertThat(paginaResultado.getTotalElements()).isEqualTo(2);
                assertThat(paginaResultado.getNumber()).isEqualTo(0);
                assertThat(paginaResultado.getSize()).isEqualTo(10);
            })
            .verifyComplete();

        verify(repositorioHistorial).findAllPaginated(0, 10);
        verify(repositorioHistorial).count();
    }

    @Test
    @DisplayName("Debe manejar historial vacío correctamente")
    void debeManejarHistorialVacio() {
        int pagina = 0;
        int tamanio = 10;

        when(repositorioHistorial.findAllPaginated(0, 10))
            .thenReturn(Flux.empty());
        when(repositorioHistorial.count())
            .thenReturn(Mono.just(0L));

        Mono<Page<CallHistory>> resultadoMono = callHistoryService.getCallHistory(pagina, tamanio);

        StepVerifier.create(resultadoMono)
            .assertNext(paginaResultado -> {
                assertThat(paginaResultado.getContent()).isEmpty();
                assertThat(paginaResultado.getTotalElements()).isZero();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Debe calcular el offset correcto para la paginación")
    void debeCalcularOffsetCorrecto() {
        int pagina = 2;
        int tamanio = 5;

        when(repositorioHistorial.findAllPaginated(10, 5))
            .thenReturn(Flux.empty());
        when(repositorioHistorial.count())
            .thenReturn(Mono.just(0L));

        callHistoryService.getCallHistory(pagina, tamanio).block();

        verify(repositorioHistorial).findAllPaginated(10, 5);
    }
}
