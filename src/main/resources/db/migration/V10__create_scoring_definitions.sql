CREATE TABLE scoring_definitions (
    id UUID PRIMARY KEY,
    scoring_key VARCHAR(160) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(60) NOT NULL,
    start_value NUMERIC(18, 4) NOT NULL DEFAULT 0,
    min_value NUMERIC(18, 4),
    max_value NUMERIC(18, 4),
    only_increase BOOLEAN NOT NULL DEFAULT FALSE,
    only_decrease BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_scoring_definitions_key UNIQUE (scoring_key),
    CONSTRAINT ck_scoring_definitions_direction
        CHECK (NOT (only_increase AND only_decrease)),
    CONSTRAINT ck_scoring_definitions_min_max
        CHECK (min_value IS NULL OR max_value IS NULL OR min_value <= max_value)
);

CREATE INDEX ix_scoring_definitions_active
    ON scoring_definitions (active);
