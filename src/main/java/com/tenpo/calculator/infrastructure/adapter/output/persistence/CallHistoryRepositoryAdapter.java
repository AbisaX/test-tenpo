package com.tenpo.calculator.infrastructure.adapter.output.persistence;

import com.tenpo.calculator.domain.model.CallHistory;
import com.tenpo.calculator.domain.port.output.CallHistoryRepositoryPort;
import com.tenpo.calculator.infrastructure.adapter.output.persistence.entity.CallHistoryEntity;
import com.tenpo.calculator.infrastructure.adapter.output.persistence.repository.CallHistoryR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallHistoryRepositoryAdapter implements CallHistoryRepositoryPort {

    private final CallHistoryR2dbcRepository repository;

    @Override
    public Mono<CallHistory> save(CallHistory callHistory) {
        log.debug("Saving call history to database");
        return repository.save(CallHistoryEntity.fromDomain(callHistory))
            .map(CallHistoryEntity::toDomain);
    }

    @Override
    public Flux<CallHistory> findAllPaginated(int offset, int limit) {
        log.debug("Finding call history with offset={}, limit={}", offset, limit);
        return repository.findAllPaginated(offset, limit)
            .map(CallHistoryEntity::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }
}
