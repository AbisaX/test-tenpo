package com.tenpo.calculator.infrastructure.adapter.output.persistence.repository;

import com.tenpo.calculator.infrastructure.adapter.output.persistence.entity.CallHistoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CallHistoryR2dbcRepository extends ReactiveCrudRepository<CallHistoryEntity, Long> {

    @Query("SELECT * FROM call_history ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    Flux<CallHistoryEntity> findAllPaginated(int offset, int limit);
}
