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
    ('c7801379-8831-4e2f-a51f-9f391c2cbdf7', 'scoreHistory', 'Score History', 'LIST_OF_NUMBER', null, 'UNION', false, now(), now()),
    ('1b7878a7-8c5e-4711-a518-65813dbd46c7', 'loyaltyTier', 'Loyalty Tier', 'TEXT', null, 'NEWEST_VALUE', false, now(), now())
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

INSERT INTO scoring_definitions (
    id,
    scoring_key,
    name,
    type,
    start_value,
    min_value,
    max_value,
    only_increase,
    only_decrease,
    active,
    created_at,
    updated_at
)
VALUES
    (
        '9a66ed12-674e-4ce9-999f-81712fe72e52',
        'engagement',
        'Engagement',
        'NUMBER',
        0,
        0,
        100,
        false,
        false,
        true,
        now(),
        now()
    ),
    (
        'd9502ef0-dfc0-4313-a978-0f5a6a8d5f6f',
        'commercialValue',
        'Commercial Value',
        'NUMBER',
        0,
        0,
        1000,
        true,
        false,
        true,
        now(),
        now()
    ),
    (
        'b9ae8ca7-a48b-45df-b09c-d5205b7354af',
        'churnRisk',
        'Churn Risk',
        'NUMBER',
        50,
        0,
        100,
        false,
        false,
        true,
        now(),
        now()
    )
ON CONFLICT (scoring_key) DO UPDATE
SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    start_value = EXCLUDED.start_value,
    min_value = EXCLUDED.min_value,
    max_value = EXCLUDED.max_value,
    only_increase = EXCLUDED.only_increase,
    only_decrease = EXCLUDED.only_decrease,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO rule_definitions (
    id,
    rule_key,
    name,
    description,
    condition_id,
    priority,
    active,
    outputs,
    created_at,
    updated_at
)
VALUES
    (
        'd432a039-b987-4f5e-bd5c-a39cf0f8d9d1',
        'vipCustomerRule',
        'VIP Customer Rule',
        'Adds VIP profile outputs when lifetimeValue is high.',
        'a123a11d-2f3c-4efc-a6d6-c3b03807e560',
        100,
        true,
        '{
            "attributes": {
                "loyaltyTier": "gold"
            },
            "tags": ["vip", "high-value"],
            "scores": {
                "engagement": {
                    "operation": "INCREASE",
                    "value": 10
                },
                "commercialValue": {
                    "operation": "INCREASE",
                    "value": 25
                }
            },
            "actions": [
                {
                    "key": "notifyCrmVip",
                    "type": "CRM_NOTIFICATION",
                    "payload": {
                        "reason": "vipCustomer"
                    }
                }
            ]
        }'::jsonb,
        now(),
        now()
    ),
    (
        '0f969639-8eb2-4b29-bd3c-b9c55cfaa9d1',
        'highValuePurchaseRule',
        'High Value Purchase Rule',
        'Tags and scores customers when they send a high value purchase event.',
        '988ae17f-8448-4ca5-92c6-c248622924be',
        110,
        true,
        '{
            "tags": ["high-value-purchase"],
            "scores": {
                "engagement": {
                    "operation": "INCREASE",
                    "value": 5
                },
                "commercialValue": {
                    "operation": "INCREASE",
                    "value": 15
                }
            },
            "actions": [
                {
                    "key": "sendHighValuePurchaseWebhook",
                    "type": "WEBHOOK",
                    "payload": {
                        "template": "highValuePurchase"
                    }
                }
            ]
        }'::jsonb,
        now(),
        now()
    )
ON CONFLICT (rule_key) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    condition_id = EXCLUDED.condition_id,
    priority = EXCLUDED.priority,
    active = EXCLUDED.active,
    outputs = EXCLUDED.outputs,
    updated_at = now();

INSERT INTO action_type_definitions (
    id,
    action_key,
    name,
    description,
    processing_channel,
    active,
    params,
    created_at,
    updated_at
)
VALUES
    (
        'e67d6c10-35b4-49d9-b165-5867b4a2c8f4',
        'WEBHOOK',
        'Webhook',
        'Sends an outbound webhook to an integration worker.',
        'action-processing-webhook',
        true,
        '[
            {
                "key": "template",
                "name": "Template",
                "type": "TEXT",
                "required": true,
                "description": "Template or routing key used by the webhook executor."
            },
            {
                "key": "webhookUrl",
                "name": "Webhook URL",
                "type": "TEXT",
                "required": false,
                "description": "Optional destination URL when not resolved by template."
            }
        ]'::jsonb,
        now(),
        now()
    ),
    (
        'fcce0899-47da-4205-a8f6-cbfa4e53a410',
        'CRM_NOTIFICATION',
        'CRM Notification',
        'Creates a CRM notification action for downstream integration.',
        'action-processing-crm-notification',
        true,
        '[
            {
                "key": "reason",
                "name": "Reason",
                "type": "TEXT",
                "required": true,
                "description": "Reason code sent to the CRM connector."
            }
        ]'::jsonb,
        now(),
        now()
    ),
    (
        '68ea0b21-5c8a-4d4f-bbe8-44e65089b061',
        'EMAIL',
        'Email',
        'Sends an email action to the email processing channel.',
        'action-processing-email',
        true,
        '[
            {
                "key": "template",
                "name": "Template",
                "type": "TEXT",
                "required": true,
                "description": "Email template key."
            },
            {
                "key": "subject",
                "name": "Subject",
                "type": "TEXT",
                "required": false,
                "description": "Optional email subject override."
            }
        ]'::jsonb,
        now(),
        now()
    ),
    (
        '2b12cddd-6f88-4489-9ebd-2814ec31f686',
        'WEB_PUSH',
        'Web Push',
        'Sends a browser web push action to the web push processing channel.',
        'action-processing-web-push',
        true,
        '[
            {
                "key": "template",
                "name": "Template",
                "type": "TEXT",
                "required": true,
                "description": "Web push template key."
            },
            {
                "key": "url",
                "name": "URL",
                "type": "TEXT",
                "required": false,
                "description": "Optional landing URL."
            }
        ]'::jsonb,
        now(),
        now()
    ),
    (
        '45b27b8c-b7c1-489c-9697-2296611e5d29',
        'APP_PUSH',
        'App Push',
        'Sends a mobile app push action to the app push processing channel.',
        'action-processing-app-push',
        true,
        '[
            {
                "key": "template",
                "name": "Template",
                "type": "TEXT",
                "required": true,
                "description": "App push template key."
            },
            {
                "key": "deeplink",
                "name": "Deeplink",
                "type": "TEXT",
                "required": false,
                "description": "Optional app deeplink."
            }
        ]'::jsonb,
        now(),
        now()
    )
ON CONFLICT (action_key) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    processing_channel = EXCLUDED.processing_channel,
    active = EXCLUDED.active,
    params = EXCLUDED.params,
    updated_at = now();

INSERT INTO webhook_templates (
    id,
    template_key,
    name,
    method,
    url,
    headers,
    body,
    active,
    created_at,
    updated_at
)
VALUES
    (
        '02d63f14-0de8-49b3-9f94-92a8649acbd6',
        'highValuePurchase',
        'High Value Purchase Webhook',
        'POST',
        'https://example.com/webhooks/high-value-purchase',
        '{
            "Content-Type": "application/json",
            "X-Source": "unomi-modern"
        }'::jsonb,
        '{
            "profileId": "{{profileId}}",
            "messageId": "{{messageId}}",
            "ruleKey": "{{ruleKey}}",
            "actionKey": "{{actionKey}}",
            "template": "{{payload.template}}"
        }',
        true,
        now(),
        now()
    )
ON CONFLICT (template_key) DO UPDATE
SET
    name = EXCLUDED.name,
    method = EXCLUDED.method,
    url = EXCLUDED.url,
    headers = EXCLUDED.headers,
    body = EXCLUDED.body,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO email_smtp_configs (
    id,
    config_key,
    name,
    host,
    port,
    username,
    password,
    from_address,
    from_name,
    auth_enabled,
    start_tls_enabled,
    active,
    created_at,
    updated_at
)
VALUES
    (
        'c5848424-a9bc-42fc-9f8d-bc5b845b8926',
        'defaultSmtp',
        'Default SMTP',
        'smtp.example.com',
        587,
        'smtp-user',
        'smtp-password',
        'noreply@example.com',
        'Unomi Modern',
        true,
        true,
        true,
        now(),
        now()
    )
ON CONFLICT (config_key) DO UPDATE
SET
    name = EXCLUDED.name,
    host = EXCLUDED.host,
    port = EXCLUDED.port,
    username = EXCLUDED.username,
    password = EXCLUDED.password,
    from_address = EXCLUDED.from_address,
    from_name = EXCLUDED.from_name,
    auth_enabled = EXCLUDED.auth_enabled,
    start_tls_enabled = EXCLUDED.start_tls_enabled,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO email_templates (
    id,
    template_key,
    name,
    smtp_config_id,
    to_address,
    subject,
    body,
    content_type,
    active,
    created_at,
    updated_at
)
VALUES
    (
        'db71cc7d-d972-4c86-9b3e-5a2ff343f52d',
        'welcomeEmail',
        'Welcome Email',
        'c5848424-a9bc-42fc-9f8d-bc5b845b8926',
        '{{payload.email}}',
        'Welcome {{payload.firstName}}',
        '<p>Hello {{payload.firstName}}, welcome to Unomi Modern.</p>',
        'text/html; charset=UTF-8',
        true,
        now(),
        now()
    )
ON CONFLICT (template_key) DO UPDATE
SET
    name = EXCLUDED.name,
    smtp_config_id = EXCLUDED.smtp_config_id,
    to_address = EXCLUDED.to_address,
    subject = EXCLUDED.subject,
    body = EXCLUDED.body,
    content_type = EXCLUDED.content_type,
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
