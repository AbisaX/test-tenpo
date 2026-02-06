package com.tenpo.calculator.domain.port.input;

import com.tenpo.calculator.domain.model.CallHistory;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface CallHistoryUseCase {

    void saveCallHistoryAsync(CallHistory callHistory);

    Mono<Page<CallHistory>> getCallHistory(int page, int size);
}
