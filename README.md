# Unomi Modern

A Spring Boot based customer data platform foundation inspired by Apache Unomi.

This first slice keeps the scope intentionally narrow:

- Customer profiles live in Elasticsearch.
- Customer events live in Elasticsearch.
- Definitions and configuration live in PostgreSQL as JSONB.
- Personalization, campaigns, scoring, and complex rule execution are out of scope for now.

## API conventions

All request and response fields use camelCase. This includes external-ingestion-style APIs, even when an API is inspired by a third-party payload that uses snake_case.

All `/api/**` endpoints require an API key in the `X-API-Key` header. The local development seed key is `dev-unomi-api-key`.

Customer attributes and event attributes must be defined in PostgreSQL before they are accepted during profile or event writes. Supported value types are `TEXT`, `NUMBER`, `DATETIME`, `LIST_OF_TEXT`, and `LIST_OF_NUMBER`. Unknown keys and values with the wrong type are ignored during upsert.

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
  "mergeStrategy": "SOURCE_PRIORITY"
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

## Architecture direction

The code starts as a modular monolith so the domain boundaries stay visible without operational overhead. As the product grows, good next modules are identity resolution, consent, segment evaluation, and event enrichment.
