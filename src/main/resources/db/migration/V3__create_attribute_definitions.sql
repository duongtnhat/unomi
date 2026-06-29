create table customer_attribute_definitions (
    id uuid primary key,
    attribute_key varchar(160) not null unique,
    name varchar(255) not null,
    value_type varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table event_attribute_definitions (
    id uuid primary key,
    attribute_key varchar(160) not null unique,
    name varchar(255) not null,
    value_type varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

insert into customer_attribute_definitions (id, attribute_key, name, value_type, created_at, updated_at)
values
    ('22fd48d4-01b2-46c4-a326-260c40c2ada5', 'age', 'Age', 'NUMBER', now(), now()),
    ('d707c964-5efe-42ce-a353-85224f302a69', 'language', 'Language', 'TEXT', now(), now()),
    ('0be5d169-6ea8-4a19-bf68-dd8f71ba5062', 'favoriteColor', 'Favorite Color', 'LIST_OF_TEXT', now(), now());

insert into event_attribute_definitions (id, attribute_key, name, value_type, created_at, updated_at)
values
    ('bfc3c527-28c4-45b9-b4c2-1a65086f77d7', 'eventGroupId', 'Event Group ID', 'TEXT', now(), now()),
    ('3dab22cc-d21e-4fd2-9639-f4274798a124', 'productId', 'Product ID', 'TEXT', now(), now()),
    ('2934ff10-891e-4b7c-8e5d-6d19568f1612', 'currency', 'Currency', 'TEXT', now(), now()),
    ('86187dbf-cd17-40a8-9a28-d8f2cf184c99', 'quantity', 'Quantity', 'NUMBER', now(), now()),
    ('772fd4a4-12d2-498f-97f5-dc4df3f3a30a', 'unitSalePrice', 'Unit Sale Price', 'NUMBER', now(), now()),
    ('962a17b4-b4f9-41de-9fe3-4633dfd56b32', 'url', 'URL', 'TEXT', now(), now());
