# Unomi Modern

A Spring Boot based customer data platform foundation inspired by Apache Unomi.

This first slice keeps the scope intentionally narrow:

- Customer profiles live in Elasticsearch.
- Customer events live in Elasticsearch.
- Definitions and configuration live in PostgreSQL as JSONB.
- Customer upsert writes, profile merging, event writes, and segment qualification run asynchronously through Kafka.
- Personalization, campaigns, scoring, and complex rule execution are out of scope for now.

## API conventions

All request and response fields use camelCase. This includes external-ingestion-style APIs, even when an API is inspired by a third-party payload that uses snake_case.

All `/api/**` endpoints require an API key in the `X-API-Key` header. The local development seed key is `dev-unomi-api-key`.

Customer attributes and event attributes must be defined in PostgreSQL before they are accepted during profile or event writes. Supported value types are `TEXT`, `NUMBER`, `DATETIME`, `LIST_OF_TEXT`, and `LIST_OF_NUMBER`. Unknown keys and values with the wrong type are ignored during upsert.

The batch customer upsert API is asynchronous. It validates each user item and publishes accepted commands to Kafka topic `customer-upsert-commands`. Processing then moves through separate Kafka consumers:

- `unomi-write-es-workers` consumes `customer-upsert-commands`, writes profile/event data to Elasticsearch, then publishes `profile-merge-commands` when `skipHook` is `false`.
- `unomi-merge-workers` consumes `profile-merge-commands`, runs profile merge, then publishes `segment-qualification-commands`.
- `unomi-segment-workers` consumes `segment-qualification-commands` and updates `segmentIds`/`segmentKeys`.

For local development, one app process runs all roles. For separate API nodes, set `UNOMI_WRITE_ES_CONSUMER_ENABLED=false`, `UNOMI_MERGE_CONSUMER_ENABLED=false`, and `UNOMI_SEGMENT_CONSUMER_ENABLED=false`. For dedicated worker services, enable only the consumer role you want that service to run.

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

## Architecture direction

The code starts as a modular monolith with Kafka-backed worker boundaries. API instances can accept traffic quickly, while worker instances can scale separately by sharing the `unomi-upsert-workers` consumer group. As the product grows, good next modules are identity resolution, consent, segment evaluation, and event enrichment.
