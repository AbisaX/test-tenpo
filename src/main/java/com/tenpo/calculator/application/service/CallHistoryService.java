package com.tenpo.calculator.application.service;

import com.tenpo.calculator.domain.model.CallHistory;
import com.tenpo.calculator.domain.port.input.CallHistoryUseCase;
import com.tenpo.calculator.domain.port.output.CallHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallHistoryService implements CallHistoryUseCase {

    private final CallHistoryRepositoryPort puertoRepositorioHistorial;

    @Override
    @Async
    public void saveCallHistoryAsync(CallHistory historialLlamada) {
        log.debug("Guardando historial de llamadas de forma asíncrona: {}", historialLlamada);
        puertoRepositorioHistorial.save(historialLlamada)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                guardado -> log.debug("Historial de llamadas guardado exitosamente con id: {}", guardado.id()),
                error -> log.error("Error al guardar historial de llamadas: {}", error.getMessage())
            );
    }

    @Override
    public Mono<Page<CallHistory>> getCallHistory(int pagina, int tamanio) {
        log.debug("Consultando historial de llamadas pagina={}, tamaño={}", pagina, tamanio);
        int desplazamiento = pagina * tamanio;

        return Mono.zip(
            puertoRepositorioHistorial.findAllPaginated(desplazamiento, tamanio).collectList(),
            puertoRepositorioHistorial.count()
        ).map(tupla -> {
            var contenido = tupla.getT1();
            var totalElementos = tupla.getT2();
            return new PageImpl<>(contenido, PageRequest.of(pagina, tamanio), totalElementos);
        });
    }
}
