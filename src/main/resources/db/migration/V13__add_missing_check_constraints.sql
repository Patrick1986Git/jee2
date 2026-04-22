UPDATE products
SET price = 0
WHERE price < 0;

UPDATE products
SET stock = 0
WHERE stock < 0;

UPDATE orders
SET total_amount = 0
WHERE total_amount < 0;

UPDATE order_items
SET quantity = 1
WHERE quantity <= 0;

UPDATE order_items
SET price = 0
WHERE price < 0;

UPDATE payments
SET amount = 0
WHERE amount < 0;

UPDATE discount_codes
SET used_count = 0
WHERE used_count < 0;

UPDATE discount_codes
SET usage_limit = 1
WHERE usage_limit IS NOT NULL AND usage_limit <= 0;

UPDATE discount_codes
SET valid_to = valid_from + INTERVAL '1 second'
WHERE valid_to <= valid_from;

UPDATE product_images
SET sort_order = 0
WHERE sort_order < 0;

ALTER TABLE products
    ADD CONSTRAINT chk_products_price_non_negative CHECK (price >= 0),
    ADD CONSTRAINT chk_products_stock_non_negative CHECK (stock >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total_amount_non_negative CHECK (total_amount >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0),
    ADD CONSTRAINT chk_order_items_price_non_negative CHECK (price >= 0);

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_amount_non_negative CHECK (amount >= 0);

ALTER TABLE discount_codes
    ADD CONSTRAINT chk_discount_codes_used_count_non_negative CHECK (used_count >= 0),
    ADD CONSTRAINT chk_discount_codes_usage_limit_positive_or_null CHECK (usage_limit IS NULL OR usage_limit > 0),
    ADD CONSTRAINT chk_discount_codes_valid_to_after_valid_from CHECK (valid_to > valid_from);

ALTER TABLE product_images
    ADD CONSTRAINT chk_product_images_sort_order_non_negative CHECK (sort_order >= 0);
