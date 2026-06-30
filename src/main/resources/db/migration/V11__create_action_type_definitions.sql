CREATE TABLE action_type_definitions (
    id UUID PRIMARY KEY,
    action_key VARCHAR(160) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    params JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_action_type_definitions_key UNIQUE (action_key),
    CONSTRAINT ck_action_type_definitions_params_array
        CHECK (jsonb_typeof(params) = 'array')
);

CREATE INDEX ix_action_type_definitions_active
    ON action_type_definitions (active);
