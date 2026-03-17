# Database notes

## Engine and local topology
- PostgreSQL is the primary database.
- Local dev DB is run via `docker-compose.yml` using a custom image from `docker/postgres`.
- The custom image copies Polish Hunspell dictionary files used by full-text-search migration logic.

## Connection model
### Development profile (`application-dev.yml`)
- URL: `jdbc:postgresql://localhost:5432/enterprise_shop_dev`
- User/password: `shop_dev` / `shop_dev`
- JPA `ddl-auto=validate` (schema must match migrations)
- Flyway enabled and pointed to `classpath:db/migration`

### Production profile (`application-prod.yml`)
- Uses `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- Also keeps `ddl-auto=validate`

## Schema ownership model
- Baseline schema is in `V1__schema.sql`.
- Later migrations extend/adjust schema for reviews, ratings, promotions, full text search, cart, product images, payment tracking, uniqueness constraints, optimistic locking, and email uniqueness handling.

## Full-text search specifics
Migration `V5__full_text_search.sql` introduces:
- `unaccent` extension
- Polish text search dictionary + configuration
- generated `products.search_vector`
- GIN index for search performance

This is why the repository ships dictionary files under `docker/postgres/tsearch_data`.
