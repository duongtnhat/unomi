create table definitions (
    id uuid primary key,
    definition_key varchar(160) not null,
    type varchar(80) not null,
    version integer not null,
    name varchar(255) not null,
    active boolean not null default true,
    payload jsonb not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create unique index ux_definitions_key_type_version
    on definitions (definition_key, type, version);

create index ix_definitions_type_active
    on definitions (type, active);
