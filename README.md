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
- `unomi-action-workers` consumes `action-execution-commands`, forwards each action to the Kafka processing channel configured on its action type, logs it, and marks the action event as `RESOLVED`.

For local development, one app process runs all roles. For separate API nodes, set `UNOMI_WRITE_ES_CONSUMER_ENABLED=false`, `UNOMI_MERGE_CONSUMER_ENABLED=false`, `UNOMI_SEGMENT_CONSUMER_ENABLED=false`, `UNOMI_RULE_CONSUMER_ENABLED=false`, and `UNOMI_ACTION_CONSUMER_ENABLED=false`. For dedicated worker services, enable only the consumer role you want that service to run.

Kafka publishing uses a PostgreSQL outbox. The API records accepted commands in `inbox_events` and enqueues Kafka messages in `outbox_events`; a scheduled outbox publisher sends them to Kafka. Each worker records completion in `processed_messages` by `messageId` and stage, so retries do not re-run the same stage.

See [Data Flow](docs/data-flow.md) for the current end-to-end pipeline and action channel registry.

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

## Architecture direction

The code starts as a modular monolith with Kafka-backed worker boundaries. API instances can accept traffic quickly, while worker instances can scale each pipeline stage separately by consumer group. As the product grows, good next modules are identity resolution, consent, connector destinations, and event enrichment.
