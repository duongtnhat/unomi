CREATE TABLE webhook_templates (
    id UUID PRIMARY KEY,
    template_key VARCHAR(160) NOT NULL,
    name VARCHAR(255) NOT NULL,
    method VARCHAR(16) NOT NULL,
    url TEXT NOT NULL,
    headers JSONB NOT NULL DEFAULT '{}'::jsonb,
    body TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_webhook_templates_key UNIQUE (template_key),
    CONSTRAINT ck_webhook_templates_headers_object
        CHECK (jsonb_typeof(headers) = 'object')
);

CREATE INDEX ix_webhook_templates_active
    ON webhook_templates (active);

CREATE TABLE webhook_calls (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    action_event_id UUID,
    message_id VARCHAR(80),
    profile_id VARCHAR(160),
    rule_key VARCHAR(160),
    action_key VARCHAR(160),
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    method VARCHAR(16) NOT NULL,
    url TEXT NOT NULL,
    request_headers JSONB NOT NULL DEFAULT '{}'::jsonb,
    request_body TEXT,
    response_status INTEGER,
    response_headers JSONB NOT NULL DEFAULT '{}'::jsonb,
    response_body TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_webhook_calls_template
        FOREIGN KEY (template_id)
        REFERENCES webhook_templates (id)
        ON DELETE RESTRICT
);

CREATE INDEX ix_webhook_calls_template_created_at
    ON webhook_calls (template_id, created_at);

CREATE INDEX ix_webhook_calls_action_event_id
    ON webhook_calls (action_event_id);

CREATE INDEX ix_webhook_calls_status_created_at
    ON webhook_calls (status, created_at);
