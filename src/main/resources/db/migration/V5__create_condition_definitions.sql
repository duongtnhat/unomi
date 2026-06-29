create table condition_definitions (
    id uuid primary key,
    condition_key varchar(160) not null,
    version integer not null,
    name varchar(255) not null,
    active boolean not null default true,
    payload jsonb not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create unique index ux_condition_definitions_key_version
    on condition_definitions (condition_key, version);

create index ix_condition_definitions_active
    on condition_definitions (active);
