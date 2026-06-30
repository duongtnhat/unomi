# Data Flow

This document describes the current Unomi Modern data flow from API ingestion to asynchronous workers, profile storage, rule execution, and action dispatching.

## Current Action Channels

The current action channels planned for implementation are:

| Action type | Processing channel |
| --- | --- |
| `WEBHOOK` | `action-processing-webhook` |
| `EMAIL` | `action-processing-email` |
| `WEB_PUSH` | `action-processing-web-push` |
| `APP_PUSH` | `action-processing-app-push` |

Each action type is stored in PostgreSQL as an action type definition. The `processingChannel` field stores the Kafka topic used by the action dispatcher for that type.

## Storage Responsibilities

Elasticsearch stores customer runtime data:

- Customer profiles
- Customer events
- Score change events

PostgreSQL stores metadata and durable pipeline state:

- API keys
- Definitions
- Customer attribute definitions
- Event attribute definitions
- Conditions
- Segments
- Rules
- Scoring definitions
- Action type definitions
- Webhook templates
- Webhook call history
- Email SMTP configs
- Email templates
- Email call history
- Rule action events
- Inbox events
- Outbox events
- Processed message IDs

Redis caches metadata that is read frequently by the runtime pipeline:

- Attribute definitions
- Conditions
- Segments
- Rules
- Scoring definitions
- Action type definitions

Kafka connects asynchronous pipeline stages. Each stage can be scaled independently by running consumers in its own consumer group.

## Customer Upsert Flow

1. A client calls `POST /api/user/v1/upsert` with `X-API-Key`.
2. The API validates the request, including identifiers, event names, and event timestamps.
3. Accepted user records are written to `inbox_events`.
4. The API writes a `customer-upsert-commands` message to `outbox_events`.
5. The scheduled outbox publisher sends pending outbox messages to Kafka.
6. The API returns `202 Accepted` with accepted message IDs.

The API does not write profile, event, merge, segment, rule, or action changes synchronously during upsert. Those steps are handled by Kafka workers.

## Elasticsearch Write Stage

1. `unomi-write-es-workers` consumes `customer-upsert-commands`.
2. The worker upserts the customer profile into Elasticsearch.
3. The worker writes customer events into Elasticsearch.
4. Unknown customer attributes and event attributes are ignored unless they are defined in PostgreSQL.
5. The worker records the message as processed for stage `WRITE_ES`.
6. If `skipHook` is `false`, it enqueues a `profile-merge-commands` message through the outbox.

## Profile Merge Stage

1. `unomi-merge-workers` consumes `profile-merge-commands`.
2. The worker loads the profile from Elasticsearch.
3. Mergeable customer attributes are evaluated using their merge priority and merge strategy.
4. The merged profile is saved back to Elasticsearch when needed.
5. The worker records the message as processed for stage `MERGE_PROFILE`.
6. It enqueues a `segment-qualification-commands` message through the outbox.

## Segment Qualification Stage

1. `unomi-segment-workers` consumes `segment-qualification-commands`.
2. The worker evaluates active segment conditions against the profile and the incoming events.
3. Matching segment IDs and segment keys are saved on the Elasticsearch profile.
4. The worker records the message as processed for stage `SEGMENT_QUALIFICATION`.
5. It enqueues a `rule-evaluation-commands` message through the outbox.

## Rule Evaluation Stage

1. `unomi-rule-workers` consumes `rule-evaluation-commands`.
2. The worker evaluates active rules in priority order.
3. Rule conditions are evaluated against profile context and event context.
4. Matching rules can emit outputs:
   - Profile attributes
   - Profile tags
   - Profile scores
   - Action events
5. Profile attributes are filtered by customer attribute definitions.
6. Score updates are filtered by active scoring definitions, bounded by min/max, and constrained by direction flags.
7. Score changes are stored on the Elasticsearch profile and also written as `scoreChanged` events in Elasticsearch.
8. Action outputs are written to PostgreSQL as `rule_action_events`.
9. Each action event receives a `trackingId` UUID. The action command carries both `trackingId` and `profileId` across all downstream consumers.
10. For each action event, the worker enqueues an `action-execution-commands` message through the outbox.
11. The worker records the message as processed for stage `RULE_EVALUATION`.

## Action Dispatch Stage

1. `unomi-action-workers` consumes `action-execution-commands`.
2. The worker loads the active action type definition by `actionType`.
3. The worker reads the action type `processingChannel`.
4. The worker logs `trackingId`, `profileId`, `actionEventId`, `actionType`, and the target processing channel.
5. The worker forwards the action command to that Kafka topic with `trackingId` and `profileId` preserved.
6. After forwarding succeeds, the action event status is updated to `RESOLVED`.
7. The worker records the action event ID as processed for stage `ACTION_EXECUTION`.

The action worker is a dispatcher only. It does not execute webhook, email, web push, or app push delivery itself. Dedicated downstream consumers subscribe to the processing channel topics. The first implemented downstream processor is the webhook worker.

## Webhook Processing Stage

1. `unomi-webhook-workers` consumes `action-processing-webhook`.
2. The worker reads `payload.template` from the action command.
3. The worker loads an active `webhook_templates` row by that template key.
4. The worker renders the template body with Mustache syntax. The render context includes:
   - Top-level payload fields
   - `payload`
   - `actionEventId`
   - `messageId`
   - `requestedAt`
   - `profileId`
   - `ruleKey`
   - `actionKey`
   - `actionType`
5. The worker calls the configured HTTP method and URL with the configured headers and rendered body.
6. Every attempt is stored in `webhook_calls`, including `trackingId`, `profileId`, request body, response status/body, and error message when the call fails.
7. The worker records the action event ID as processed for stage `WEBHOOK_PROCESSING`.

## Email Processing Stage

1. `unomi-email-workers` consumes `action-processing-email`.
2. The worker reads `payload.template` from the action command.
3. The worker loads an active `email_templates` row by that template key.
4. The template references an `email_smtp_configs` row with SMTP host, port, credentials, and sender address.
5. The worker renders recipient, subject, and body with Mustache syntax. The render context is the same action context used by webhook processing.
6. The worker sends the email through the configured SMTP server.
7. Every attempt is stored in `email_calls`, including `trackingId`, `profileId`, recipient, subject, body, and error message when the send fails.
8. The worker records the action event ID as processed for stage `EMAIL_PROCESSING`.

## Idempotency And Durability

The pipeline uses three PostgreSQL tables for durability:

- `inbox_events` records accepted API input.
- `outbox_events` records messages that must be published to Kafka.
- `processed_messages` records completed processing stages.

Each worker checks `processed_messages` before executing a stage. If Kafka retries a message, the stage is skipped when the stage has already completed for that message ID.

Action execution uses the rule action event ID as the idempotency key because one customer upsert message can produce multiple actions.

## Scaling Model

The system starts as a modular monolith, but worker roles can be split into separate deployments:

- API-only nodes disable all consumers and the outbox publisher when needed.
- Write workers enable only `UNOMI_WRITE_ES_CONSUMER_ENABLED`.
- Merge workers enable only `UNOMI_MERGE_CONSUMER_ENABLED`.
- Segment workers enable only `UNOMI_SEGMENT_CONSUMER_ENABLED`.
- Rule workers enable only `UNOMI_RULE_CONSUMER_ENABLED`.
- Action dispatcher workers enable only `UNOMI_ACTION_CONSUMER_ENABLED`.
- Channel-specific processors subscribe to `action-processing-webhook`, `action-processing-email`, `action-processing-web-push`, and `action-processing-app-push`.
