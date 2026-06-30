# Unomi Modern

A Spring Boot based customer data platform foundation inspired by Apache Unomi.

This first slice keeps the scope intentionally narrow:

- Customer profiles live in Elasticsearch.
- Customer events live in Elasticsearch.
- Definitions and configuration live in PostgreSQL as JSONB.
- Customer upsert writes, profile merging, event writes, segment qualification, and rule evaluation run asynchronously through Kafka.
- Personalization and campaigns are out of scope for now, but rule-driven attributes, tags, actions, and scoring are supported.

## API conventions

All request and response fields use camelCase. This includes external-ingestion-style APIs, even when an API is inspired by a third-party payload that uses snake_case.

All `/api/**` endpoints require an API key in the `X-API-Key` header. The local development seed key is `dev-unomi-api-key`.

Customer attributes and event attributes must be defined in PostgreSQL before they are accepted during profile or event writes. Supported value types are `TEXT`, `NUMBER`, `DATETIME`, `LIST_OF_TEXT`, and `LIST_OF_NUMBER`. Unknown keys and values with the wrong type are ignored during upsert.

The batch customer upsert API is asynchronous. It validates each user item and publishes accepted commands to Kafka topic `customer-upsert-commands`. Processing then moves through separate Kafka consumers:

- `unomi-write-es-workers` consumes `customer-upsert-commands`, writes profile/event data to Elasticsearch, then publishes `profile-merge-commands` when `skipHook` is `false`.
- `unomi-merge-workers` consumes `profile-merge-commands`, runs profile merge, then publishes `segment-qualification-commands`.
- `unomi-segment-workers` consumes `segment-qualification-commands`, updates `segmentIds`/`segmentKeys`, then publishes `rule-evaluation-commands`.
- `unomi-rule-workers` consumes `rule-evaluation-commands`, evaluates active rules, updates profile attributes/tags/scores, writes `scoreChanged` events to Elasticsearch, and stores action events in PostgreSQL.
- `unomi-action-workers` consumes `action-execution-commands`, logs the action for now, and marks the action event as `RESOLVED`.

For local development, one app process runs all roles. For separate API nodes, set `UNOMI_WRITE_ES_CONSUMER_ENABLED=false`, `UNOMI_MERGE_CONSUMER_ENABLED=false`, `UNOMI_SEGMENT_CONSUMER_ENABLED=false`, `UNOMI_RULE_CONSUMER_ENABLED=false`, and `UNOMI_ACTION_CONSUMER_ENABLED=false`. For dedicated worker services, enable only the consumer role you want that service to run.

Kafka publishing uses a PostgreSQL outbox. The API records accepted commands in `inbox_events` and enqueues Kafka messages in `outbox_events`; a scheduled outbox publisher sends them to Kafka. Each worker records completion in `processed_messages` by `messageId` and stage, so retries do not re-run the same stage.

## Run locally

Start the infrastructure:

```bash
docker compose up -d
```

Run the app:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

Seed local PostgreSQL metadata:

```powershell
Get-Content scripts\seed-postgresql-sample-data.sql | docker exec -i unomi-postgres-1 psql -U unomi -d unomi -v ON_ERROR_STOP=1
```

Seed local Elasticsearch sample profiles and events:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\seed-elasticsearch-sample-data.ps1
```

## API sketch

Create or update a customer profile:

```http
POST /api/profiles
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "profileKey": "email:ada@example.com",
  "anonymousId": "anon-123",
  "email": "ada@example.com",
  "properties": {
    "age": 30,
    "language": "en_US",
    "favoriteColor": ["green"]
  }
}
```

Ingest an event:

```http
POST /api/events
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "profileId": "profile-id-from-response",
  "eventType": "pageView",
  "source": "web",
  "payload": {
    "url": "/pricing"
  }
}
```

Upsert customer info in a batch shape inspired by Insider One:

```http
POST /api/user/v1/upsert
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "skipHook": false,
  "users": [
    {
      "identifiers": {
        "email": "ada@example.com",
        "uuid": "customer-001",
        "custom": {
          "loyaltyId": "L-100"
        }
      },
      "attributes": {
        "age": 30,
        "language": "en_US",
        "favoriteColor": ["green"]
      },
      "events": [
        {
          "eventName": "purchase",
          "timestamp": "2026-06-30T00:00:00Z",
          "eventParams": {
            "eventGroupId": "ORDER123",
            "productId": "SKU-1",
            "currency": "USD",
            "quantity": 1,
            "unitSalePrice": 89.9
          }
        }
      ]
    }
  ]
}
```

Successful upsert responses return HTTP `202 Accepted` with `messageIds` for the Kafka commands that were accepted.

Create a customer attribute definition:

```http
POST /api/customer-attributes
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "lifetimeValue",
  "name": "Lifetime Value",
  "type": "NUMBER",
  "mergePriority": 10,
  "mergeStrategy": "SOURCE_PRIORITY",
  "pii": false
}
```

Create an event attribute definition:

```http
POST /api/event-attributes
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "campaignId",
  "name": "Campaign ID",
  "type": "TEXT"
}
```

Create or update a definition:

```http
POST /api/definitions
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "vip-customers",
  "type": "SEGMENT",
  "version": 1,
  "name": "VIP Customers",
  "active": true,
  "payload": {
    "conditions": [
      {
        "property": "lifetimeValue",
        "operator": "gte",
        "value": 1000
      }
    ]
  }
}
```

Evaluate a condition:

```http
POST /api/conditions/evaluate
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "condition": {
    "type": "boolean",
    "parameters": {
      "operator": "and"
    },
    "conditions": [
      {
        "type": "profileProperty",
        "parameters": {
          "propertyName": "properties.age",
          "operator": "gte",
          "value": 18
        }
      },
      {
        "type": "eventType",
        "parameters": {
          "operator": "equals",
          "value": "purchase"
        }
      }
    ]
  },
  "profile": {
    "properties": {
      "age": 30
    }
  },
  "event": {
    "eventType": "purchase"
  }
}
```

Create or update a condition definition:

```http
POST /api/conditions
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "adult-purchase-condition",
  "version": 1,
  "name": "Adult purchase condition",
  "active": true,
  "payload": {
    "type": "profileProperty",
    "parameters": {
      "propertyName": "properties.age",
      "operator": "gte",
      "value": 18
    }
  }
}
```

Create or update a segment definition from a condition:

```http
POST /api/segments
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "adultBuyers",
  "name": "Adult Buyers",
  "description": "Customers aged at least 18",
  "conditionId": "condition-id-from-response",
  "active": true
}
```

Create or update a rule definition from a condition:

```http
POST /api/rules
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "vipCustomerRule",
  "name": "VIP Customer Rule",
  "description": "Adds VIP outputs when lifetimeValue is high.",
  "conditionId": "condition-id-from-response",
  "priority": 100,
  "active": true,
  "outputs": {
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
        "operation": "SET",
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
  }
}
```

Create a scoring definition used by rule outputs:

```http
POST /api/scorings
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "engagement",
  "name": "Engagement",
  "type": "NUMBER",
  "startValue": 0,
  "minValue": 0,
  "maxValue": 100,
  "onlyIncrease": false,
  "onlyDecrease": false,
  "active": true
}
```

Manage scores attached to one profile:

```http
GET /api/scorings/profiles/profile-1/scores
X-API-Key: dev-unomi-api-key
```

```http
POST /api/scorings/profiles/profile-1/scores/engagement
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "operation": "INCREASE",
  "value": 10
}
```

```http
DELETE /api/scorings/profiles/profile-1/scores
X-API-Key: dev-unomi-api-key
```

Create an action type and inspect its params:

```http
POST /api/action-types
X-API-Key: dev-unomi-api-key
Content-Type: application/json

{
  "key": "WEBHOOK",
  "name": "Webhook",
  "description": "Sends an outbound webhook to an integration worker.",
  "active": true,
  "params": [
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
      "required": false
    }
  ]
}
```

```http
GET /api/action-types/{id}/params
X-API-Key: dev-unomi-api-key
```

## Architecture direction

The code starts as a modular monolith with Kafka-backed worker boundaries. API instances can accept traffic quickly, while worker instances can scale each pipeline stage separately by consumer group. As the product grows, good next modules are identity resolution, consent, connector destinations, and event enrichment.
