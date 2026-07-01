ALTER TABLE email_templates
    ALTER COLUMN to_address DROP NOT NULL;

ALTER TABLE email_calls
    ALTER COLUMN to_address DROP NOT NULL;
