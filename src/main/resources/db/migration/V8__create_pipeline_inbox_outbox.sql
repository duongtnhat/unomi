CREATE TABLE inbox_events (
    id UUID PRIMARY KEY,
    message_id VARCHAR(80) NOT NULL UNIQUE,
    source VARCHAR(120) NOT NULL,
    payload_type VARCHAR(240) NOT NULL,
    payload JSONB NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_inbox_events_source_received_at
    ON inbox_events (source, received_at);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    message_id VARCHAR(80) NOT NULL,
    topic VARCHAR(180) NOT NULL,
    message_key VARCHAR(180) NOT NULL,
    payload_type VARCHAR(240) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(40) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_outbox_events_status_next_attempt_at
    ON outbox_events (status, next_attempt_at);

CREATE INDEX ix_outbox_events_message_id
    ON outbox_events (message_id);

CREATE TABLE processed_messages (
    id UUID PRIMARY KEY,
    message_id VARCHAR(80) NOT NULL,
    stage VARCHAR(80) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_processed_messages_message_stage UNIQUE (message_id, stage)
);

CREATE INDEX ix_processed_messages_stage_processed_at
    ON processed_messages (stage, processed_at);
