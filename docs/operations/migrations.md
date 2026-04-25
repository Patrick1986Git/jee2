# Flyway migrations

## Location and naming
- Location: `src/main/resources/db/migration`
- Naming pattern: `V<version>__<description>.sql`

## Current migration sequence
- `V1` baseline schema (users, roles, categories, products, orders, payments)
- `V2` product reviews
- `V3` product rating aggregates
- `V4` discount/promotion codes
- `V5` product full-text search (Polish)
- `V6` cart and cart items
- `V7` product images
- `V8` payment intent tracking fields
- `V9` one-payment-per-order uniqueness
- `V10` optimistic locking columns
- `V11` product constraint rename harmonization
- `V12` case-insensitive unique index on user email
- `V13` missing numeric/date check constraints hardening
- `V14` status/payment-method check constraints hardening
- `V15` remove unused audit/soft-delete columns from `order_items`

## Rules for future changes
1. Add a new migration for every schema change; do not edit old migrations.
2. Keep migrations idempotent where practical (`IF EXISTS` / `IF NOT EXISTS` guards when sensible).
3. Align constraints/index names with existing conventions (`uq_`, `idx_`, `fk_`).
4. Keep entity mappings and migration changes synchronized in the same PR.

## Timestamp/audit style policy for new migrations

To keep migration diffs reviewable and avoid behavior changes before a dedicated `TIMESTAMPTZ` rollout:

1. Use `TIMESTAMP` as the current default for new audit columns until a dedicated `TIMESTAMPTZ` rollout is planned.
2. Use `DEFAULT CURRENT_TIMESTAMP` for `created_at` when DB default is needed.
3. Do not use `NOW()` in new migrations (equivalent in PostgreSQL, but we keep one canonical style).
4. Keep `updated_at`/`deleted_at` nullable and managed by application logic unless a migration explicitly needs DB-side behavior.
5. Do not rewrite historical migrations only for timestamp syntax consistency; document and fix forward.

## Validation commands
```bash
mvn test
mvn -q -DskipTests compile
```

For broader release confidence:
```bash
mvn clean test
```
