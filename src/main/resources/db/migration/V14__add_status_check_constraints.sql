UPDATE orders
SET status = UPPER(BTRIM(status))
WHERE status IS NOT NULL
  AND status <> UPPER(BTRIM(status))
  AND UPPER(BTRIM(status)) IN ('NEW', 'PAID', 'SHIPPED', 'CANCELLED');

UPDATE payments
SET status = UPPER(BTRIM(status))
WHERE status IS NOT NULL
  AND status <> UPPER(BTRIM(status))
  AND UPPER(BTRIM(status)) IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

UPDATE payments
SET payment_method = UPPER(BTRIM(payment_method))
WHERE payment_method IS NOT NULL
  AND payment_method <> UPPER(BTRIM(payment_method))
  AND UPPER(BTRIM(payment_method)) IN ('STRIPE');

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status_allowed
    CHECK (status IN ('NEW', 'PAID', 'SHIPPED', 'CANCELLED'));

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_status_allowed
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    ADD CONSTRAINT chk_payments_payment_method_allowed
    CHECK (payment_method IN ('STRIPE'));
