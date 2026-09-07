# Observability

## Request correlation

`RequestIdFilter` provides lightweight HTTP/log correlation for every request.

| Behavior | Current implementation |
| --- | --- |
| Incoming header | Reads `X-Request-Id`. |
| Accepted caller value | Trimmed, non-blank, printable ASCII, max 100 characters. |
| Generated value | UUID when the header is missing or invalid. |
| MDC key | `requestId`. |
| Response header | Always sets `X-Request-Id` to the accepted/generated value. |
| CORS exposure | `X-Request-Id` is exposed to browsers. |

## Logging

The console log pattern includes the request correlation id:

```text
requestId=%X{requestId}
```

Operational policy:

- Do not log JWT tokens, passwords, card data, Stripe secrets, or webhook secrets.
- Do not add high-cardinality request/user/payment identifiers as metric tags.
- Request/response body logging is intentionally not part of the current baseline.

## Actuator and Prometheus

Configured actuator web exposure includes `health`, `info`, `metrics`, and `prometheus`.

| Endpoint | Access | Purpose |
| --- | --- | --- |
| `/actuator/health` | Public | Health status. Details are shown when authorized. |
| `/actuator/info` | Admin | Application info. |
| `/actuator/metrics` | Admin | Metrics index and individual meter lookup. |
| `/actuator/prometheus` | Admin | Prometheus scrape endpoint provided by the Prometheus registry. |

## Application metric inventory

Counters are monotonic process-lifetime event totals. Calculate rates in the monitoring/query layer; a counter is not a
current backlog measurement. Gauges are point-in-time database state, return zero when no matching row exists, and
clamp age to zero if the application clock is behind a stored timestamp.

| Metric | Type | Producer | Tags and finite values | Meaning |
| --- | --- | --- | --- | --- |
| `shop.checkout.total` | Counter | `OrderCheckoutProcessor` | `result`: `attempt`, `success`, `failure` | Checkout attempts and terminal in-process outcomes. |
| `shop.payment_intent.total` | Counter | `PaymentServiceImpl` | `result`: `created`, `reused`, `failed` | Payment-intent initialization outcomes. |
| `shop.webhook.total` | Counter | `PaymentServiceImpl` | `result`: `received`, `processed`, `ignored`, `duplicate`, `failed` | Validly parsed arrivals and webhook handling outcomes. A signature/payload rejection contributes only `failed`, not `received`. |
| `shop.business_exception.total` | Counter | `GlobalExceptionHandler` | `error_code`: repository-defined business error codes; `status_class`: `4xx`, `5xx`, `other` | Business exceptions translated to API errors. Error messages are never tags. |
| `shop.order.reservation_expiration.total` | Counter | `ReservationExpirationProcessor` | `outcome`: `claimed`, `terminal_noop`, `provider_succeeded`, `provider_already_canceled`, `provider_canceled`, `provider_pending`, `failed`, `retry`, `retry_exhausted` | Reservation-expiration processing transitions and outcomes. A claim can increment more than one outcome. |
| `shop.order.reservation_expiration.inventory_units_released` | Counter | `ReservationExpirationProcessor` | none | Inventory units released by successful reservation expiration. |
| `shop.order.reservation_expiration.recovery.total` | Counter | `ReservationExpirationRecoveryService` | `outcome`: `requeued`, `terminal_noop` | ADMIN recovery outcomes for failed expiration work. |
| `shop.order.reservation_expiration.failed.count` | Gauge | `ReservationExpirationMetrics` | none | Current terminal failed reservation-work rows. |
| `shop.order.reservation_expiration.failed.oldest.age.seconds` | Gauge | `ReservationExpirationMetrics` | none | Age since `failed_at` of the oldest current failed reservation-work row. |
| `shop.outbox.actionable.count` | Gauge | `OutboxEventMetrics` | none | Current PENDING events whose `next_attempt_at` is absent or due. Future retries are excluded. |
| `shop.outbox.actionable.oldest.age.seconds` | Gauge | `OutboxEventMetrics` | none | Time since the oldest current event became actionable: `next_attempt_at` for retries, otherwise `created_at`. |
| `shop.outbox.dead_letter.count` | Gauge | `OutboxEventMetrics` | none | Current DEAD_LETTER events requiring operator investigation or requeue. |
| `shop.outbox.dead_letter.oldest.age.seconds` | Gauge | `OutboxEventMetrics` | none | Time since the oldest current dead letter's terminal `last_attempt_at`. |
| `shop.notification.actionable.count` | Gauge | `NotificationDeliveryMetrics` | none | Current due PENDING notifications plus PROCESSING notifications with expired claims. Future deliveries and live claims are excluded. |
| `shop.notification.actionable.oldest.age.seconds` | Gauge | `NotificationDeliveryMetrics` | none | Time since the oldest matching notification became actionable: due/retry time (or creation) for PENDING, claim expiry for PROCESSING. |
| `shop.notification.failed.count` | Gauge | `NotificationDeliveryMetrics` | none | Current terminal FAILED notifications requiring operator investigation or requeue. |
| `shop.notification.failed.oldest.age.seconds` | Gauge | `NotificationDeliveryMetrics` | none | Time since the oldest current failed notification's terminal `last_attempt_at`. |

All tag value sets above are defined by repository code. No tag contains a user, order, payment, provider-event,
email, JWT subject, request, arbitrary provider value, exception message, payload, or notification content. Tests use a
`SimpleMeterRegistry` for exact names/types/tags and PostgreSQL repository tests prove the scalar backlog query semantics.

The application also relies on Spring Boot's standard HTTP/JVM/process/data-source meters and standard health
contributors. Their names and labels are framework-owned rather than repository-defined; deployments should pin and
review the framework version before treating those as a durable external contract.

## Asynchronous degradation and alert contract

Backlog is observed through metrics, not health. A short backlog must not remove a replica from traffic, and a database
failure continues to affect readiness without automatically failing liveness. An HTTP-ready process with a stalled
scheduler is revealed when actionable count remains non-zero and oldest actionable age keeps increasing. Terminal
count and age distinguish work requiring investigation/requeue from retryable work.

`app.outbox.processing.enabled=false` and `app.notification.delivery.enabled=false` may be intentional maintenance
choices. They are not health failures and no enabled-state gauge is added: deployment configuration validation is the
authoritative control. If either worker is unintentionally disabled while work arrives, its actionable age exposes the
resulting degradation. Likewise, `app.notification.smtp.enabled=false` intentionally selects the no-op sender and is
not inherently an error; a production deployment that requires email must validate that configuration separately.

This repository owns no production alert manager and defines no numeric SLOs, thresholds, routes, or dashboards.
Deployment owners should select objectives and alert conditions such as:

- actionable outbox or notification age exceeding the selected processing/delivery objective;
- dead-letter or failed work remaining non-zero beyond the approved investigation window;
- sustained checkout, payment-intent, webhook, reservation-processing, or recovery failure rates exceeding the
  selected error budget.

The counters distinguish observed traffic/outcomes, but cannot alone distinguish no traffic from a broken upstream
route. Webhook `failed`, `ignored`, and `duplicate` rates separate those observed outcomes; they do not measure Stripe
availability or guarantee end-to-end provider delivery. The deployment owns checkout availability, payment-intent
success, webhook delay, outbox delay, notification delay, and reservation recovery objectives and must combine these
application signals with traffic/platform evidence appropriate to its environment.

## Query cost and operator diagnostics

Each backlog gauge executes a scalar `COUNT` or `MIN`; no entity, JSON outbox payload, notification body, recipient, or
error body is loaded. Existing indexes support the predicates: outbox `(status, next_attempt_at)`,
`(status, created_at)`, and `(status, last_attempt_at)`; notification `(status, next_attempt_at, created_at)`,
`(claim_expires_at)`, and `(status, last_attempt_at)`. No migration or additional write-amplifying index is required.

Current reservation retry exhaustion and Stripe failures have warning/error logs with identifiers useful for
investigation. Outbox and notification failures persist bounded workflow state and error details for protected ADMIN
inspection but do not currently emit dedicated failure logs. This avoids logging outbox payloads, notification bodies,
recipients, raw webhook bodies, secrets, or client secrets; metrics provide the proactive signal and protected records
provide drill-down context.

Tagging rules:

- Keep tags low-cardinality.
- Do not tag metrics with user ids, order ids, payment ids, emails, JWT subjects, request ids, or raw exception messages.
- Use logs with `requestId` for per-request debugging rather than high-cardinality metric labels.

## Smoke checks

Use an authenticated admin request for protected actuator endpoints.

```bash
curl -i http://localhost:8080/actuator/health
curl -i -H "Authorization: Bearer <admin-token>" http://localhost:8080/actuator/prometheus
```
