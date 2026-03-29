# Local development

## Prerequisites
- Java 17+
- Maven
- Docker + Docker Compose

## 1) Start local PostgreSQL
```bash
docker-compose up -d
```

This starts `enterprise-shop-db` on `localhost:5432`.

## 2) Prepare database user/permissions
The repository includes a helper SQL script at `scripts/db-setup.sql` with commands for creating `enterprise_shop_dev` and `shop_dev`.

Apply equivalent SQL with your local PostgreSQL superuser if your instance is fresh.

## 3) Run the application
```bash
mvn spring-boot:run
```

Default active profile is `dev` (from `application.yml`).

## 4) Validate startup
- Root probe: `GET http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Important environment variables
For real payment flow testing, provide Stripe keys via environment:
- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`
- `STRIPE_PUBLIC_KEY`

JWT can be overridden with:
- `JWT_SECRET`
