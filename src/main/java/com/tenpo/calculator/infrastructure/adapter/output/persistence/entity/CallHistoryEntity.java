package com.tenpo.calculator.infrastructure.adapter.output.persistence.entity;

import com.tenpo.calculator.domain.model.CallHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("call_history")
public class CallHistoryEntity {

    @Id
    private Long id;

    @Column("timestamp")
    private LocalDateTime timestamp;

    @Column("endpoint")
    private String endpoint;

    @Column("http_method")
    private String httpMethod;

    @Column("parameters")
    private String parameters;

    @Column("response")
    private String response;

    @Column("status_code")
    private Integer statusCode;

    @Column("success")
    private boolean success;

    public static CallHistoryEntity fromDomain(CallHistory historialLlamada) {
        return CallHistoryEntity.builder()
            .id(historialLlamada.id())
            .timestamp(historialLlamada.timestamp())
            .endpoint(historialLlamada.endpoint())
            .httpMethod(historialLlamada.httpMethod())
            .parameters(historialLlamada.parameters())
            .response(historialLlamada.response())
            .statusCode(historialLlamada.statusCode())
            .success(historialLlamada.success())
            .build();
    }

    public CallHistory toDomain() {
        return new CallHistory(
            this.id,
            this.timestamp,
            this.endpoint,
            this.httpMethod,
            this.parameters,
            this.response,
            this.statusCode,
            this.success
        );
    }
}
