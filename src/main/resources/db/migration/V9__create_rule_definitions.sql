CREATE TABLE rule_definitions (
    id UUID PRIMARY KEY,
    rule_key VARCHAR(160) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    condition_id UUID NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    outputs JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_rule_definitions_key UNIQUE (rule_key),
    CONSTRAINT fk_rule_definitions_condition
        FOREIGN KEY (condition_id)
        REFERENCES condition_definitions (id)
        ON DELETE RESTRICT
);

CREATE INDEX ix_rule_definitions_active_priority
    ON rule_definitions (active, priority);

CREATE INDEX ix_rule_definitions_condition_id
    ON rule_definitions (condition_id);

CREATE TABLE rule_action_events (
    id UUID PRIMARY KEY,
    rule_id UUID NOT NULL,
    rule_key VARCHAR(160) NOT NULL,
    message_id VARCHAR(80) NOT NULL,
    profile_id VARCHAR(160) NOT NULL,
    action_key VARCHAR(160) NOT NULL,
    action_type VARCHAR(80) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_rule_action_events_rule
        FOREIGN KEY (rule_id)
        REFERENCES rule_definitions (id)
        ON DELETE CASCADE
);

CREATE INDEX ix_rule_action_events_profile_id_created_at
    ON rule_action_events (profile_id, created_at);

CREATE INDEX ix_rule_action_events_status_created_at
    ON rule_action_events (status, created_at);

DELETE FROM definitions
WHERE type = 'RULE';
