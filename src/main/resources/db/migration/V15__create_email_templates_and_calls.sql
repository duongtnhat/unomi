CREATE TABLE email_smtp_configs (
    id UUID PRIMARY KEY,
    config_key VARCHAR(160) NOT NULL,
    name VARCHAR(255) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    username VARCHAR(255),
    password TEXT,
    from_address VARCHAR(320) NOT NULL,
    from_name VARCHAR(255),
    auth_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    start_tls_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_email_smtp_configs_key UNIQUE (config_key)
);

CREATE INDEX ix_email_smtp_configs_active
    ON email_smtp_configs (active);

CREATE TABLE email_templates (
    id UUID PRIMARY KEY,
    template_key VARCHAR(160) NOT NULL,
    name VARCHAR(255) NOT NULL,
    smtp_config_id UUID NOT NULL,
    to_address TEXT NOT NULL,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    content_type VARCHAR(80) NOT NULL DEFAULT 'text/html',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_email_templates_key UNIQUE (template_key),
    CONSTRAINT fk_email_templates_smtp_config
        FOREIGN KEY (smtp_config_id)
        REFERENCES email_smtp_configs (id)
        ON DELETE RESTRICT
);

CREATE INDEX ix_email_templates_active
    ON email_templates (active);

CREATE INDEX ix_email_templates_smtp_config_id
    ON email_templates (smtp_config_id);

CREATE TABLE email_calls (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    smtp_config_id UUID NOT NULL,
    action_event_id UUID,
    tracking_id UUID,
    message_id VARCHAR(80),
    profile_id VARCHAR(160),
    rule_key VARCHAR(160),
    action_key VARCHAR(160),
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    from_address VARCHAR(320) NOT NULL,
    to_address TEXT NOT NULL,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_email_calls_template
        FOREIGN KEY (template_id)
        REFERENCES email_templates (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_email_calls_smtp_config
        FOREIGN KEY (smtp_config_id)
        REFERENCES email_smtp_configs (id)
        ON DELETE RESTRICT
);

CREATE INDEX ix_email_calls_template_created_at
    ON email_calls (template_id, created_at);

CREATE INDEX ix_email_calls_smtp_config_created_at
    ON email_calls (smtp_config_id, created_at);

CREATE INDEX ix_email_calls_action_event_id
    ON email_calls (action_event_id);

CREATE INDEX ix_email_calls_tracking_id
    ON email_calls (tracking_id);

CREATE INDEX ix_email_calls_status_created_at
    ON email_calls (status, created_at);
