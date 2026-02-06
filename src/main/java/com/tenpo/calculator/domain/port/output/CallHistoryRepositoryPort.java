package com.tenpo.calculator.domain.port.output;

import com.tenpo.calculator.domain.model.CallHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CallHistoryRepositoryPort {

    Mono<CallHistory> save(CallHistory callHistory);

    Flux<CallHistory> findAllPaginated(int offset, int limit);

    Mono<Long> count();
}
