CREATE TABLE segment_definitions (
    id UUID PRIMARY KEY,
    segment_key VARCHAR(160) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    condition_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ux_segment_definitions_key UNIQUE (segment_key),
    CONSTRAINT fk_segment_definitions_condition
        FOREIGN KEY (condition_id)
        REFERENCES condition_definitions (id)
        ON DELETE RESTRICT
);

CREATE INDEX ix_segment_definitions_active ON segment_definitions (active);
CREATE INDEX ix_segment_definitions_condition_id ON segment_definitions (condition_id);
