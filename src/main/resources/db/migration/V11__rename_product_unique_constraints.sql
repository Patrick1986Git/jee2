DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'products' AND constraint_name = 'products_sku_key'
    ) THEN
        ALTER TABLE products RENAME CONSTRAINT products_sku_key TO uq_products_sku;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'products' AND constraint_name = 'products_slug_key'
    ) THEN
        ALTER TABLE products RENAME CONSTRAINT products_slug_key TO uq_products_slug;
    END IF;
END $$;
