ALTER TABLE payments
    ADD COLUMN provider_payment_id VARCHAR(255),
    ADD COLUMN client_secret VARCHAR(500);

CREATE INDEX idx_payments_provider_payment_id ON payments(provider_payment_id);
