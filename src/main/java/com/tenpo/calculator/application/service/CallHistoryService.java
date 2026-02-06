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

    private final CallHistoryRepositoryPort callHistoryRepositoryPort;

    @Override
    @Async
    public void saveCallHistoryAsync(CallHistory callHistory) {
        log.debug("Guardando historial de llamadas de forma asíncrona: {}", callHistory);
        callHistoryRepositoryPort.save(callHistory)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                saved -> log.debug("Historial de llamadas guardado exitosamente con id: {}", saved.id()),
                error -> log.error("Error al guardar historial de llamadas: {}", error.getMessage())
            );
    }

    @Override
    public Mono<Page<CallHistory>> getCallHistory(int page, int size) {
        log.debug("Consultando historial de llamadas pagina={}, tamaño={}", page, size);
        int offset = page * size;

        return Mono.zip(
            callHistoryRepositoryPort.findAllPaginated(offset, size).collectList(),
            callHistoryRepositoryPort.count()
        ).map(tuple -> {
            var content = tuple.getT1();
            var totalElements = tuple.getT2();
            return new PageImpl<>(content, PageRequest.of(page, size), totalElements);
        });
    }
}
