ALTER TABLE rule_action_events
    ADD COLUMN tracking_id UUID;

CREATE INDEX ix_rule_action_events_tracking_id
    ON rule_action_events (tracking_id);

ALTER TABLE webhook_calls
    ADD COLUMN tracking_id UUID;

CREATE INDEX ix_webhook_calls_tracking_id
    ON webhook_calls (tracking_id);
