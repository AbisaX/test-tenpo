CREATE TABLE IF NOT EXISTS call_history (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    endpoint VARCHAR(500) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    parameters TEXT,
    response TEXT,
    status_code INTEGER,
    success BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_call_history_timestamp ON call_history(timestamp DESC);
CREATE INDEX idx_call_history_endpoint ON call_history(endpoint);
