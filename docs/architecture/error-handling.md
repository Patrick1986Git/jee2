# Error handling

## API error contract
Errors are returned as `ApiError` with a consistent structure:
- `status` (HTTP code)
- `message` (human-readable)
- `errorCode` (machine-readable when available)
- `errors` (optional details map/object)
- `timestamp`

## Primary strategy
- Domain/business failures should extend `BusinessException`.
- `BusinessException` carries HTTP status + optional error code.
- `GlobalExceptionHandler` translates both business and framework exceptions into `ApiError`.

## Important mappings in `GlobalExceptionHandler`
- `BusinessException` -> status from exception, fallback error code `UNKNOWN_BUSINESS_ERROR`.
- `MethodArgumentNotValidException` / `ConstraintViolationException` -> `400 VALIDATION_FAILED` with field/global details.
- `MethodArgumentTypeMismatchException` / malformed request body / missing params -> `400 REQUEST_INVALID`.
- `ObjectOptimisticLockingFailureException` -> `409 OPTIMISTIC_LOCK_CONFLICT`.
- `AccessDeniedException` -> `403 ACCESS_DENIED`.
- `NoHandlerFoundException` -> `404 ENDPOINT_NOT_FOUND`.
- Unhandled `Exception` -> generic `500` response without internal details.

## Practical guidance for new module exceptions
- Add exception class under the owning module’s `exception` package.
- Prefer specific error codes (e.g., `PRODUCT_NOT_FOUND`) for client behavior.
- Let exceptions bubble to the global handler instead of returning ad hoc error payloads in controllers.
