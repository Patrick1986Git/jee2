# Module map

## Business modules (`com.company.shop.module`)

### cart
Purpose: authenticated shopper cart lifecycle.
- APIs: `/api/v1/me/cart` and item operations.
- Owns `Cart`, `CartItem`, cart DTOs, mapper, repository, service, and stock-related cart exceptions.

### category
Purpose: category tree and catalog classification.
- Public APIs: list + lookup by slug.
- Admin APIs: CRUD under `/api/v1/admin/categories`.
- Owns category hierarchy validation and duplicate/slug exceptions.

### order
Purpose: checkout, order history, admin order listing, Stripe webhook handling.
- Shopper APIs: `/api/v1/me/orders`, `/api/v1/me/orders/checkout`.
- Shared access API: `/api/v1/orders/{id}`.
- Admin API: `/api/v1/admin/orders`.
- Webhook API: `/api/v1/webhooks/stripe`.
- Owns payment/order entities and payment processing exception model.

### product
Purpose: product browsing, search, reviews, and admin product management.
- Public APIs: list, search, slug lookup, by-category lookup, review listing.
- Auth APIs: create/delete review.
- Admin APIs: CRUD under `/api/v1/admin/products`.
- Owns product aggregate, review model, image model, and specification-based querying.

### system
Purpose: health-like application status and root API probe.
- APIs: `/api/v1` and `/api/v1/system/status`.

### user
Purpose: user profile and admin user management.
- Authenticated profile API: `/api/v1/me`.
- Admin APIs: `/api/v1/admin/users`.
- Owns user-role model, user-specific error codes, and user mappers/services.

## Shared infrastructure modules

### security
- Auth endpoints under `/api/v1/auth`.
- Login/register service flow.
- JWT creation/validation and auth filter.
- Current user abstraction + security constants.

### common
- Base entities (`BaseEntity`, `AuditableEntity`, `SoftDeleteEntity`).
- Shared business exception superclass.
- API error DTO and global exception translation.

### config
- Security filter chain.
- OpenAPI metadata setup.
- Auditing and SQL function contributor configuration.

### validation
- Custom password-match annotation + validator.
