-- Sample PostgreSQL metadata for local development.
-- Run after Flyway migrations have completed.
--
-- Docker example:
--   docker exec -i unomi-postgres-1 psql -U unomi -d unomi < scripts/seed-postgresql-sample-data.sql

BEGIN;

INSERT INTO api_keys (id, name, key_hash, active, expires_at, created_at)
VALUES
    (
        '8f8e1aa4-4131-48bc-91f6-9b35aa0fc616',
        'local-dev',
        'ff1b4879e2ce8ccdac7256fbd01d3d2893665a6bffd0b17d6e6e12d24f84119f',
        true,
        null,
        now()
    )
ON CONFLICT (key_hash) DO UPDATE
SET
    name = EXCLUDED.name,
    active = EXCLUDED.active,
    expires_at = EXCLUDED.expires_at;

INSERT INTO customer_attribute_definitions (
    id,
    attribute_key,
    name,
    value_type,
    merge_priority,
    merge_strategy,
    pii,
    created_at,
    updated_at
)
VALUES
    ('22fd48d4-01b2-46c4-a326-260c40c2ada5', 'age', 'Age', 'NUMBER', null, null, false, now(), now()),
    ('d707c964-5efe-42ce-a353-85224f302a69', 'language', 'Language', 'TEXT', null, null, false, now(), now()),
    ('0be5d169-6ea8-4a19-bf68-dd8f71ba5062', 'favoriteColor', 'Favorite Color', 'LIST_OF_TEXT', null, 'UNION', false, now(), now()),
    ('52f684f1-2e42-41eb-91bc-c7c5b47cb7f1', 'email', 'Email', 'TEXT', 10, 'SOURCE_PRIORITY', true, now(), now()),
    ('af2e49e3-37eb-4377-9b72-4a63dbe7d501', 'phoneNumber', 'Phone Number', 'TEXT', 20, 'SOURCE_PRIORITY', true, now(), now()),
    ('9c1f7ea1-36d2-4a8f-85d9-b1e6e75b24cf', 'loyaltyId', 'Loyalty ID', 'TEXT', 30, 'SOURCE_PRIORITY', true, now(), now()),
    ('d3da4fe8-f8cc-495a-9c6c-5f9af59e348f', 'firstName', 'First Name', 'TEXT', null, null, true, now(), now()),
    ('551138cc-fca1-47f6-a049-f4fdc25af9bb', 'lastName', 'Last Name', 'TEXT', null, null, true, now(), now()),
    ('ff3cce6e-9baf-4449-aa21-0cb8176f5a02', 'birthDate', 'Birth Date', 'DATETIME', null, null, true, now(), now()),
    ('84ff87b6-b960-40f4-a5b8-f5294ecb6c6c', 'lifetimeValue', 'Lifetime Value', 'NUMBER', null, 'SUM', false, now(), now()),
    ('de011fc7-6b88-4a8a-97b2-d5fa036ed3bd', 'lastSeenAt', 'Last Seen At', 'DATETIME', null, 'NEWEST_VALUE', false, now(), now()),
    ('8ef4ec2f-4385-449a-8851-f689733dba27', 'interestTags', 'Interest Tags', 'LIST_OF_TEXT', null, 'UNION', false, now(), now()),
    ('db5c68a7-5897-4946-a187-32e3083c3a19', 'preferredCategories', 'Preferred Categories', 'LIST_OF_TEXT', null, 'UNION', false, now(), now()),
    ('c7801379-8831-4e2f-a51f-9f391c2cbdf7', 'scoreHistory', 'Score History', 'LIST_OF_NUMBER', null, 'UNION', false, now(), now())
ON CONFLICT (attribute_key) DO UPDATE
SET
    name = EXCLUDED.name,
    value_type = EXCLUDED.value_type,
    merge_priority = EXCLUDED.merge_priority,
    merge_strategy = EXCLUDED.merge_strategy,
    pii = EXCLUDED.pii,
    updated_at = now();

INSERT INTO event_attribute_definitions (
    id,
    attribute_key,
    name,
    value_type,
    created_at,
    updated_at
)
VALUES
    ('bfc3c527-28c4-45b9-b4c2-1a65086f77d7', 'eventGroupId', 'Event Group ID', 'TEXT', now(), now()),
    ('3dab22cc-d21e-4fd2-9639-f4274798a124', 'productId', 'Product ID', 'TEXT', now(), now()),
    ('2934ff10-891e-4b7c-8e5d-6d19568f1612', 'currency', 'Currency', 'TEXT', now(), now()),
    ('86187dbf-cd17-40a8-9a28-d8f2cf184c99', 'quantity', 'Quantity', 'NUMBER', now(), now()),
    ('772fd4a4-12d2-498f-97f5-dc4df3f3a30a', 'unitSalePrice', 'Unit Sale Price', 'NUMBER', now(), now()),
    ('962a17b4-b4f9-41de-9fe3-4633dfd56b32', 'url', 'URL', 'TEXT', now(), now()),
    ('bc53dd52-e711-4e42-a1d9-3f20d97ce1d8', 'campaignId', 'Campaign ID', 'TEXT', now(), now()),
    ('09606c29-5a8c-4772-bf83-0a7df1d2527d', 'sourceMedium', 'Source Medium', 'TEXT', now(), now()),
    ('a327cb83-1496-45e4-b5a9-c4edb2674a37', 'cartValue', 'Cart Value', 'NUMBER', now(), now()),
    ('6f56fe47-311d-41b7-b90b-650265bb0895', 'discountCodes', 'Discount Codes', 'LIST_OF_TEXT', now(), now()),
    ('81d7f9fb-e80f-4995-a762-2b904c7027a7', 'occurredAtClient', 'Occurred At Client', 'DATETIME', now(), now())
ON CONFLICT (attribute_key) DO UPDATE
SET
    name = EXCLUDED.name,
    value_type = EXCLUDED.value_type,
    updated_at = now();

INSERT INTO condition_definitions (
    id,
    condition_key,
    version,
    name,
    active,
    payload,
    created_at,
    updated_at
)
VALUES
    (
        '34d10109-ff21-4ffc-99c4-76419d4ad63a',
        'adultCustomer',
        1,
        'Adult Customer',
        true,
        '{
            "type": "profileProperty",
            "parameters": {
                "propertyName": "properties.age",
                "operator": "gte",
                "value": 18
            }
        }'::jsonb,
        now(),
        now()
    ),
    (
        'a123a11d-2f3c-4efc-a6d6-c3b03807e560',
        'vipCustomer',
        1,
        'VIP Customer',
        true,
        '{
            "type": "profileProperty",
            "parameters": {
                "propertyName": "properties.lifetimeValue",
                "operator": "gte",
                "value": 1000
            }
        }'::jsonb,
        now(),
        now()
    ),
    (
        '988ae17f-8448-4ca5-92c6-c248622924be',
        'highValuePurchase',
        1,
        'High Value Purchase',
        true,
        '{
            "type": "boolean",
            "parameters": {
                "operator": "and"
            },
            "conditions": [
                {
                    "type": "eventType",
                    "parameters": {
                        "operator": "equals",
                        "value": "purchase"
                    }
                },
                {
                    "type": "eventProperty",
                    "parameters": {
                        "propertyName": "payload.unitSalePrice",
                        "operator": "gte",
                        "value": 250
                    }
                }
            ]
        }'::jsonb,
        now(),
        now()
    ),
    (
        '0f9271b5-b7e5-4f4e-a89b-1ac9a7436ef3',
        'abandonedCart',
        1,
        'Abandoned Cart',
        true,
        '{
            "type": "boolean",
            "parameters": {
                "operator": "and"
            },
            "conditions": [
                {
                    "type": "eventType",
                    "parameters": {
                        "operator": "equals",
                        "value": "cartAbandoned"
                    }
                },
                {
                    "type": "eventProperty",
                    "parameters": {
                        "propertyName": "payload.cartValue",
                        "operator": "gte",
                        "value": 100
                    }
                }
            ]
        }'::jsonb,
        now(),
        now()
    ),
    (
        '89c72f24-3aa9-43ca-af9c-f3cb20de976a',
        'vietnameseLanguage',
        1,
        'Vietnamese Language',
        true,
        '{
            "type": "profileProperty",
            "parameters": {
                "propertyName": "properties.language",
                "operator": "equals",
                "value": "vi"
            }
        }'::jsonb,
        now(),
        now()
    )
ON CONFLICT (condition_key, version) DO UPDATE
SET
    name = EXCLUDED.name,
    active = EXCLUDED.active,
    payload = EXCLUDED.payload,
    updated_at = now();

INSERT INTO segment_definitions (
    id,
    segment_key,
    name,
    description,
    condition_id,
    active,
    created_at,
    updated_at
)
VALUES
    (
        '38b66935-b497-4d88-8745-c41c7ab1bc1e',
        'adultCustomers',
        'Adult Customers',
        'Customers with age greater than or equal to 18.',
        '34d10109-ff21-4ffc-99c4-76419d4ad63a',
        true,
        now(),
        now()
    ),
    (
        'e170a1fa-573c-43dc-9f74-b548a49697bc',
        'vipCustomers',
        'VIP Customers',
        'Customers with lifetimeValue greater than or equal to 1000.',
        'a123a11d-2f3c-4efc-a6d6-c3b03807e560',
        true,
        now(),
        now()
    ),
    (
        '20b81d52-7765-4d6b-a484-8ec0699c2769',
        'highValuePurchasers',
        'High Value Purchasers',
        'Customers who sent a purchase event with a high unit sale price.',
        '988ae17f-8448-4ca5-92c6-c248622924be',
        true,
        now(),
        now()
    ),
    (
        'c7f37151-4b27-48df-93e0-3adf714f8b42',
        'abandonedCartCustomers',
        'Abandoned Cart Customers',
        'Customers who abandoned carts worth at least 100.',
        '0f9271b5-b7e5-4f4e-a89b-1ac9a7436ef3',
        true,
        now(),
        now()
    ),
    (
        '693b972a-e133-42b0-8c64-3b1df7e2e573',
        'vietnameseCustomers',
        'Vietnamese Customers',
        'Customers whose language attribute is vi.',
        '89c72f24-3aa9-43ca-af9c-f3cb20de976a',
        true,
        now(),
        now()
    )
ON CONFLICT (segment_key) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    condition_id = EXCLUDED.condition_id,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO definitions (
    id,
    definition_key,
    type,
    version,
    name,
    active,
    payload,
    created_at,
    updated_at
)
VALUES
    (
        'b989407b-99ea-4fe3-841c-14930710832f',
        'web-storefront',
        'SOURCE',
        1,
        'Web Storefront',
        true,
        '{
            "channel": "web",
            "description": "Default web storefront source"
        }'::jsonb,
        now(),
        now()
    ),
    (
        '0188e2a2-9580-45e2-8d5d-6f1468d86826',
        'email-marketing-consent',
        'CONSENT',
        1,
        'Email Marketing Consent',
        true,
        '{
            "purpose": "emailMarketing",
            "defaultGranted": false,
            "piiKeys": ["email", "firstName", "lastName"]
        }'::jsonb,
        now(),
        now()
    ),
    (
        '0726e8fe-d04c-42de-938b-17e8ec99841d',
        'high-value-purchase-rule',
        'RULE',
        1,
        'High Value Purchase Rule',
        true,
        '{
            "conditionKey": "highValuePurchase",
            "segmentKey": "highValuePurchasers"
        }'::jsonb,
        now(),
        now()
    ),
    (
        '1732cb89-dcb2-4513-8efc-bb673db769fa',
        'sample-operational-note',
        'OTHER',
        1,
        'Sample Operational Note',
        true,
        '{
            "note": "Sample metadata used by local development scripts."
        }'::jsonb,
        now(),
        now()
    )
ON CONFLICT (definition_key, type, version) DO UPDATE
SET
    name = EXCLUDED.name,
    active = EXCLUDED.active,
    payload = EXCLUDED.payload,
    updated_at = now();

COMMIT;
