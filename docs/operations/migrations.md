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

## Validation commands
```bash
mvn test
mvn -q -DskipTests compile
```

For broader release confidence:
```bash
mvn clean test
```
