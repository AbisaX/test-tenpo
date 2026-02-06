package com.tenpo.calculator.infrastructure.adapter.output.persistence;

import com.tenpo.calculator.domain.model.CallHistory;
import com.tenpo.calculator.infrastructure.adapter.output.persistence.entity.CallHistoryEntity;
import com.tenpo.calculator.infrastructure.adapter.output.persistence.repository.CallHistoryR2dbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CallHistoryRepositoryAdapter - Tests Unitarios")
class CallHistoryRepositoryAdapterTest {

    @Mock
    private CallHistoryR2dbcRepository r2dbcRepository;

    private CallHistoryRepositoryAdapter adapter;

    @BeforeEach
    void configurar() {
        adapter = new CallHistoryRepositoryAdapter(r2dbcRepository);
    }

    @Test
    @DisplayName("Debe guardar historial de llamadas y retornar modelo de dominio")
    void debeGuardarHistorialYRetornarModeloDominio() {
        // Arrange (Preparación)
        CallHistory historial = CallHistory.create(
            "/api/v1/calculator/calculate",
            "POST",
            "{}",
            "{}",
            200,
            true
        );

        CallHistoryEntity entidadGuardada = CallHistoryEntity.builder()
            .id(1L)
            .timestamp(historial.timestamp())
            .endpoint(historial.endpoint())
            .httpMethod(historial.httpMethod())
            .parameters(historial.parameters())
            .response(historial.response())
            .statusCode(historial.statusCode())
            .success(historial.success())
            .build();

        when(r2dbcRepository.save(any(CallHistoryEntity.class)))
            .thenReturn(Mono.just(entidadGuardada));

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(adapter.save(historial))
            .assertNext(guardado -> {
                assertThat(guardado.id()).isEqualTo(1L);
                assertThat(guardado.endpoint()).isEqualTo("/api/v1/calculator/calculate");
                assertThat(guardado.httpMethod()).isEqualTo("POST");
                assertThat(guardado.success()).isTrue();
            })
            .verifyComplete();

        verify(r2dbcRepository).save(any(CallHistoryEntity.class));
    }

    @Test
    @DisplayName("Debe encontrar historial de llamadas paginado")
    void debeEncontrarHistorialPaginado() {
        // Arrange (Preparación)
        CallHistoryEntity entidad1 = CallHistoryEntity.builder()
            .id(1L)
            .timestamp(LocalDateTime.now())
            .endpoint("/api/v1/calculator/calculate")
            .httpMethod("POST")
            .parameters("{}")
            .response("{}")
            .statusCode(200)
            .success(true)
            .build();

        CallHistoryEntity entidad2 = CallHistoryEntity.builder()
            .id(2L)
            .timestamp(LocalDateTime.now())
            .endpoint("/api/v1/history")
            .httpMethod("GET")
            .parameters("{}")
            .response("[]")
            .statusCode(200)
            .success(true)
            .build();

        when(r2dbcRepository.findAllPaginated(0, 10))
            .thenReturn(Flux.just(entidad1, entidad2));

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(adapter.findAllPaginated(0, 10))
            .assertNext(historial -> {
                assertThat(historial.id()).isEqualTo(1L);
                assertThat(historial.endpoint()).isEqualTo("/api/v1/calculator/calculate");
            })
            .assertNext(historial -> {
                assertThat(historial.id()).isEqualTo(2L);
                assertThat(historial.endpoint()).isEqualTo("/api/v1/history");
            })
            .verifyComplete();

        verify(r2dbcRepository).findAllPaginated(0, 10);
    }

    @Test
    @DisplayName("Debe retornar el conteo de todos los registros")
    void debeRetornarConteoDeRegistros() {
        // Arrange (Preparación)
        when(r2dbcRepository.count()).thenReturn(Mono.just(100L));

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(adapter.count())
            .assertNext(conteo -> assertThat(conteo).isEqualTo(100L))
            .verifyComplete();

        verify(r2dbcRepository).count();
    }

    @Test
    @DisplayName("Debe manejar conjunto de resultados vacío")
    void debeManejarResultadosVacios() {
        // Arrange (Preparación)
        when(r2dbcRepository.findAllPaginated(0, 10))
            .thenReturn(Flux.empty());

        // Act & Assert (Acción y Verificación)
        StepVerifier.create(adapter.findAllPaginated(0, 10))
            .verifyComplete();
    }
}
