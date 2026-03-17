# API overview

This is a controller-level inventory of currently implemented HTTP endpoints.

## Auth and system
| Method | Path | Access |
|---|---|---|
| GET | `/api/v1` | Public |
| GET | `/api/v1/system/status` | Authenticated |
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/auth/register` | Public |

## Users
| Method | Path | Access |
|---|---|---|
| GET | `/api/v1/me` | Authenticated |
| GET | `/api/v1/admin/users` | Admin |
| GET | `/api/v1/admin/users/{id}` | Admin |
| PUT | `/api/v1/admin/users/{id}` | Admin |
| DELETE | `/api/v1/admin/users/{id}` | Admin |

## Categories
| Method | Path | Access |
|---|---|---|
| GET | `/api/v1/categories` | Public |
| GET | `/api/v1/categories/slug/{slug}` | Public |
| GET | `/api/v1/admin/categories/{id}` | Admin |
| POST | `/api/v1/admin/categories` | Admin |
| PUT | `/api/v1/admin/categories/{id}` | Admin |
| DELETE | `/api/v1/admin/categories/{id}` | Admin |

## Products and reviews
| Method | Path | Access |
|---|---|---|
| GET | `/api/v1/products` | Public |
| GET | `/api/v1/products/category/{categoryId}` | Public |
| GET | `/api/v1/products/slug/{slug}` | Public |
| GET | `/api/v1/products/search` | Public |
| GET | `/api/v1/products/{productId}/reviews` | Public |
| POST | `/api/v1/reviews` | Authenticated |
| DELETE | `/api/v1/reviews/{reviewId}` | Authenticated |
| GET | `/api/v1/admin/products/{id}` | Admin |
| POST | `/api/v1/admin/products` | Admin |
| PUT | `/api/v1/admin/products/{id}` | Admin |
| DELETE | `/api/v1/admin/products/{id}` | Admin |

## Cart
| Method | Path | Access |
|---|---|---|
| GET | `/api/v1/me/cart` | Authenticated |
| POST | `/api/v1/me/cart/items` | Authenticated |
| PATCH | `/api/v1/me/cart/items/{productId}` | Authenticated |
| DELETE | `/api/v1/me/cart/items/{productId}` | Authenticated |
| DELETE | `/api/v1/me/cart` | Authenticated |

## Orders and payments
| Method | Path | Access |
|---|---|---|
| GET | `/api/v1/me/orders` | Authenticated |
| POST | `/api/v1/me/orders/checkout` | Authenticated |
| GET | `/api/v1/orders/{id}` | Authenticated |
| GET | `/api/v1/admin/orders` | Admin |
| POST | `/api/v1/webhooks/stripe` | Public (signature-verified by Stripe secret) |

## OpenAPI UI
- Swagger UI: `/swagger-ui.html` (redirects to springdoc UI path)
- JSON spec: `/api-docs` and `/v3/api-docs/**`
