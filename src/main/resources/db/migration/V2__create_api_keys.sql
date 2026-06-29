create table api_keys (
    id uuid primary key,
    name varchar(160) not null,
    key_hash varchar(64) not null unique,
    active boolean not null default true,
    expires_at timestamptz,
    last_used_at timestamptz,
    created_at timestamptz not null
);

create index ix_api_keys_active_expires_at
    on api_keys (active, expires_at);

insert into api_keys (id, name, key_hash, active, created_at)
values (
    '8f8e1aa4-4131-48bc-91f6-9b35aa0fc616',
    'local-dev',
    'ff1b4879e2ce8ccdac7256fbd01d3d2893665a6bffd0b17d6e6e12d24f84119f',
    true,
    now()
);
