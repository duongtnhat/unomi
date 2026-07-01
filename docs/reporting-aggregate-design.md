# Reporting Aggregate Design Proposal

This document captures a proposed reporting data model for tracking delivery rates, received rates, and future reporting needs across email, webhook, push, rules, segments, scoring, and other channels.

This is a design note only. It is not implemented yet.

## Goals

- Track email send rate and received/delivered rate.
- Support provider callbacks such as delivered, bounced, dropped, opened, and clicked.
- Keep raw event history for audit and idempotency.
- Maintain aggregate tables for fast reporting queries.
- Reuse the same aggregate model for future reports beyond email.

## Important Distinction

`email_calls.status = SUCCESS` only means the system successfully sent the email request through SMTP or an email provider.

It does not prove that the recipient mailbox received the email.

To measure actual delivery or receipt, the system needs delivery events from an email provider, such as:

- `accepted`
- `delivered`
- `bounced`
- `dropped`
- `deferred`
- `opened`
- `clicked`

## Raw Email Delivery Events

Raw events should be stored first. This gives the system an audit trail and allows aggregate jobs to be replayed.

```sql
CREATE TABLE email_delivery_events (
    id UUID PRIMARY KEY,
    event_id VARCHAR(160) NOT NULL UNIQUE,
    email_call_id UUID REFERENCES email_calls(id),
    tracking_id UUID,
    profile_id VARCHAR(160),

    provider VARCHAR(80),
    event_type VARCHAR(40) NOT NULL,
    event_time TIMESTAMPTZ NOT NULL,

    template_key VARCHAR(160),
    smtp_config_key VARCHAR(160),
    campaign_key VARCHAR(160),
    rule_key VARCHAR(160),
    action_key VARCHAR(160),

    recipient_domain VARCHAR(160),
    error_code VARCHAR(80),
    error_reason TEXT,

    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

Suggested indexes:

```sql
CREATE INDEX ix_email_delivery_events_event_time
    ON email_delivery_events (event_time);

CREATE INDEX ix_email_delivery_events_tracking_id
    ON email_delivery_events (tracking_id);

CREATE INDEX ix_email_delivery_events_profile_id_event_time
    ON email_delivery_events (profile_id, event_time);

CREATE INDEX ix_email_delivery_events_type_time
    ON email_delivery_events (event_type, event_time);
```

## Generic Aggregate Table

Use a generic aggregate table instead of one report-specific table per use case.

This allows the same structure to support email delivery, webhook delivery, push delivery, rule activation, score changes, segment qualification, and other future reports.

```sql
CREATE TABLE report_metric_aggregates (
    id UUID PRIMARY KEY,

    metric_date DATE NOT NULL,
    bucket_start TIMESTAMPTZ NOT NULL,
    bucket_size VARCHAR(20) NOT NULL,

    report_type VARCHAR(80) NOT NULL,
    metric_name VARCHAR(80) NOT NULL,

    dimension_hash VARCHAR(64) NOT NULL,
    dimensions JSONB NOT NULL,

    value_count BIGINT NOT NULL DEFAULT 0,
    value_sum NUMERIC(20, 4),
    value_min NUMERIC(20, 4),
    value_max NUMERIC(20, 4),

    first_event_at TIMESTAMPTZ,
    last_event_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT ux_report_metric_aggregates_bucket_metric_dimensions
        UNIQUE (bucket_start, bucket_size, report_type, metric_name, dimension_hash)
);
```

Suggested indexes:

```sql
CREATE INDEX ix_report_metric_aggregates_type_bucket
    ON report_metric_aggregates (report_type, bucket_size, bucket_start);

CREATE INDEX ix_report_metric_aggregates_metric_bucket
    ON report_metric_aggregates (metric_name, bucket_size, bucket_start);

CREATE INDEX ix_report_metric_aggregates_dimensions_gin
    ON report_metric_aggregates USING GIN (dimensions);
```

Example dimensions:

```json
{
  "templateKey": "welcomeEmail",
  "provider": "smtp",
  "recipientDomain": "gmail.com",
  "ruleKey": "welcomeRule",
  "actionType": "EMAIL"
}
```

## Email Metrics

Recommended email metrics:

```text
email.queued
email.sendAttempted
email.sent
email.accepted
email.delivered
email.bounced
email.dropped
email.deferred
email.opened
email.clicked
email.failed
```

Recommended dimensions:

```text
templateKey
smtpConfigKey
provider
recipientDomain
campaignKey
ruleKey
actionKey
actionType
```

## Derived Rates

Rates should usually be calculated at query time from aggregate counters, not stored as permanent facts.

Examples:

```text
sendRate = email.sent / email.sendAttempted
acceptRate = email.accepted / email.sent
deliveryRate = email.delivered / email.sent
bounceRate = email.bounced / email.sent
openRate = email.opened / email.delivered
clickRate = email.clicked / email.delivered
```

This avoids stale rate values when late provider events arrive.

## Aggregation Checkpoints

If aggregation is handled asynchronously, keep a checkpoint table per aggregator.

```sql
CREATE TABLE report_aggregation_checkpoints (
    id UUID PRIMARY KEY,
    aggregator_name VARCHAR(120) NOT NULL UNIQUE,
    last_event_time TIMESTAMPTZ,
    last_event_id UUID,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

## Suggested Flow

```text
email consumer
  -> write email_calls
  -> write email_delivery_events: sent or failed

email provider webhook
  -> write email_delivery_events: accepted, delivered, bounced, opened, clicked

report aggregator worker
  -> read raw delivery events
  -> upsert counters into report_metric_aggregates

report API
  -> query report_metric_aggregates
  -> calculate rates from counters
```

## Extending Beyond Email

The same aggregate table can support other reports:

```text
webhookDelivery
pushDelivery
segmentMembership
ruleActivation
scoreChange
profileMerge
```

Examples:

```text
report_type = webhookDelivery
metric_name = webhook.success

report_type = ruleActivation
metric_name = rule.matched

report_type = scoreChange
metric_name = score.increased
```

Each report can choose its own dimensions while still using the same aggregate table shape.
