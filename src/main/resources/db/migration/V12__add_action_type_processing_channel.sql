ALTER TABLE action_type_definitions
    ADD COLUMN processing_channel VARCHAR(255);

UPDATE action_type_definitions
SET processing_channel = 'action-processing-' || lower(replace(action_key, '_', '-'))
WHERE processing_channel IS NULL;

ALTER TABLE action_type_definitions
    ALTER COLUMN processing_channel SET NOT NULL;
