# StripeWebhookController — focused WebMvcTest plan

## 1) Controller-only analysis (no service internals)

`StripeWebhookController` contains a single endpoint and only HTTP-layer behavior:

- **Route:** `POST /api/v1/webhooks/stripe`
- **Input binding:**
  - raw request body as `String payload` (`@RequestBody`)
  - required header `Stripe-Signature` (`@RequestHeader("Stripe-Signature")`)
- **Delegation:** calls `paymentService.handleWebhook(payload, sigHeader)`
- **Success response:** `200 OK`, empty body (`ResponseEntity<Void>`)

What this means for MVC scope:

- It should test **request mapping + argument binding + delegation + exception-to-HTTP translation**.
- It should **not** retest Stripe verification/business logic already covered in `PaymentServiceImplWebhookTest`.

## 2) Best WebMvcTest scope

Use a narrow MVC slice that boots only this controller and exception translation.

### Recommended class setup

- Class name: `StripeWebhookControllerWebMvcTest`
- Location: `src/test/java/com/company/shop/module/order/controller/`

Suggested annotations/dependencies:

- `@WebMvcTest(controllers = StripeWebhookController.class)`
- `@AutoConfigureMockMvc(addFilters = false)`
- `@Import(GlobalExceptionHandler.class)`
- `@MockitoBean PaymentService paymentService`
- `@Autowired MockMvc mockMvc`

Why this scope is best for this PR:

- Keeps tests focused on the controller contract only.
- Avoids pulling JWT/security filter concerns into a controller-contract PR (those are already covered in `SecurityConfigWebMvcTest`).
- Still validates standardized error payloads via the real `GlobalExceptionHandler`.

## 3) Highest-value scenarios (priority order)

### P0 — valid webhook request delegates and returns 200

**Test name:** `handleStripeWebhook_shouldReturnOkAndDelegateWhenRequestIsValid`

- POST `/api/v1/webhooks/stripe`
- include header `Stripe-Signature: sig_test`
- include body (raw JSON string is enough)
- expect `200 OK`
- verify `paymentService.handleWebhook(payload, "sig_test")` called exactly once

### P0 — missing Stripe-Signature header returns 400 and does not call service

**Test name:** `handleStripeWebhook_shouldReturnBadRequestWhenStripeSignatureHeaderMissing`

- same POST and body, but omit `Stripe-Signature`
- expect `400 Bad Request`
- verify `paymentService.handleWebhook(...)` **never** called

### P1 — invalid payload/signature business error maps to ApiError contract

**Test name:** `handleStripeWebhook_shouldReturnBadRequestApiErrorWhenSignatureInvalid`

- mock `paymentService.handleWebhook(...)` to throw `WebhookSignatureInvalidException`
- expect `400 Bad Request`
- assert response body contract:
  - `$.status = 400`
  - `$.errorCode = "STRIPE_WEBHOOK_SIGNATURE_INVALID"`
  - `$.message` present (or exact message if fixed)
  - `$.timestamp` present

### P1 — processing error maps to 500 ApiError contract

**Test name:** `handleStripeWebhook_shouldReturnInternalServerErrorApiErrorWhenWebhookProcessingFails`

- mock `paymentService.handleWebhook(...)` to throw `WebhookProcessingException`
- expect `500 Internal Server Error`
- assert body:
  - `$.status = 500`
  - `$.errorCode = "STRIPE_WEBHOOK_PROCESSING_ERROR"`
  - `$.message` present
  - `$.timestamp` present

### P2 — unexpected runtime exception fallback mapping

**Test name:** `handleStripeWebhook_shouldReturnInternalServerErrorFallbackWhenUnexpectedExceptionOccurs`

- mock `paymentService.handleWebhook(...)` to throw `RuntimeException`
- expect `500 Internal Server Error`
- assert fallback contract from `GlobalExceptionHandler`:
  - `$.status = 500`
  - `$.message = "An unexpected server error occurred. Please contact support if the problem persists."`
  - `$.errorCode` is `null`
  - `$.timestamp` present

## 4) Expected status + response contract matrix

| Scenario | Expected status | Body expectation |
|---|---:|---|
| Valid request | `200` | Empty body |
| Missing `Stripe-Signature` header | `400` | Framework 400 (body may vary depending on resolver/advice path) |
| `WebhookSignatureInvalidException` | `400` | `ApiError` with `STRIPE_WEBHOOK_SIGNATURE_INVALID` |
| `WebhookProcessingException` | `500` | `ApiError` with `STRIPE_WEBHOOK_PROCESSING_ERROR` |
| Unexpected `RuntimeException` | `500` | fallback `ApiError`, `errorCode = null` |

## 5) Suggested small-commit implementation order

1. **Create test class + wiring only**
   - `@WebMvcTest`, `MockMvc`, `@MockitoBean PaymentService`, `@Import(GlobalExceptionHandler.class)`
2. **Add happy-path delegation test (P0 #1)**
   - proves endpoint mapping and service delegation
3. **Add missing-header test (P0 #2)**
   - proves required header contract
4. **Add business-error mapping tests (P1 #3 and #4)**
   - proves webhook-specific exception contract returned to client
5. **Add generic fallback test (P2 #5)**
   - proves resilience and stable 500 contract
6. **Run only focused tests, then full module tests as needed**
   - keep PR small and reviewable

## 6) Out of scope for this controller PR

- Stripe event parsing/verification rules
- payment/order persistence side effects
- idempotency details

Those remain in `PaymentServiceImplWebhookTest` and service/domain tests.
