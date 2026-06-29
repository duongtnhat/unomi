alter table customer_attribute_definitions
    add column if not exists merge_priority integer,
    add column if not exists merge_strategy varchar(60);
