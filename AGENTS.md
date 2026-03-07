# AGENTS.md

## Project overview
This repository contains a modular Spring Boot backend for an enterprise shop application.

The codebase is organized by business modules under:
`com.company.shop.module`

Main modules:
- cart
- category
- order
- product
- system
- user

There is also a shared infrastructure layer:
- `common` for shared exceptions and base entities
- `config` for Spring configuration
- `security` for authentication, authorization, JWT, and current-user handling
- `validation` for custom validation annotations

## Primary goals when modifying this project
- Preserve the current modular package structure.
- Keep business logic inside the correct module.
- Reuse existing patterns before introducing new abstractions.
- Avoid large refactors unless explicitly requested.
- Prefer consistency with the existing codebase over cleverness.

## Package placement rules
- New business functionality must be placed in the correct module under `com.company.shop.module.<module-name>`.
- Shared technical code belongs in `common`, `config`, `security`, or `validation` only when it is truly cross-cutting.
- Do not move existing classes across modules unless explicitly required.
- Do not create generic utility classes unless there is a clear repeated need.

## Layering conventions
Within each module, follow the existing package structure where applicable:
- `controller`
- `dto`
- `entity`
- `exception`
- `mapper`
- `repository`
- `service`
- `specification` (when query logic requires it)

Rules:
- Controllers handle HTTP concerns only.
- Services contain business logic.
- Repositories handle persistence.
- Mappers convert between entities and DTOs.
- Entities should not contain controller or transport concerns.
- DTOs should not contain persistence logic.

## Existing architectural patterns to preserve
- DTO-based API design
- Dedicated mapper classes/interfaces
- Domain-specific exceptions
- Centralized exception handling in `GlobalExceptionHandler`
- Constructor injection
- Module-oriented package organization
- Spring Security with JWT-based authentication
- Flyway-based database migrations

## Exception handling rules
- Reuse `BusinessException` patterns where appropriate.
- Prefer specific domain exceptions over generic `RuntimeException`.
- Keep error handling compatible with `ApiError` and `GlobalExceptionHandler`.
- Do not swallow exceptions silently.
- Do not introduce inconsistent error response formats.
- When adding a new business exception, place it in the appropriate module's `exception` package unless it is truly shared.

## Validation rules
- Reuse Bean Validation annotations where possible.
- Use custom validation only when built-in validation is insufficient.
- Keep validation responsibilities clear:
  - request shape validation in DTOs
  - business rule validation in services/entities where appropriate

## Security rules
- Respect the current security design in `com.company.shop.security`.
- Do not weaken authentication or authorization rules.
- Reuse `CurrentUserProvider`, `SecurityCurrentUserProvider`, `EmailNormalizer`, and existing security constants where appropriate.
- Do not hardcode secrets, credentials, tokens, or API keys.
- JWT-related changes must remain consistent with the current filter/provider setup.

## Database and persistence rules
- The project uses Flyway migrations under `src/main/resources/db/migration`.
- Any schema change must be introduced as a new migration file. Do not edit old migrations unless explicitly requested.
- Preserve naming consistency for migrations.
- Keep JPA mappings aligned with existing conventions.
- Avoid unnecessary eager fetching.
- Respect optimistic locking and current uniqueness constraints.

## Mapping rules
- Reuse existing mapper patterns.
- Keep mapping logic out of controllers where possible.
- If MapStruct or generated mappers are already in use, remain consistent with the current approach.
- Do not duplicate mapping logic across services and controllers unnecessarily.

## Testing rules
- Every non-trivial code change should include tests or updates to tests.
- Prefer unit tests for service/domain logic.
- Add integration tests only when necessary.
- Follow the existing JUnit 5, AssertJ, and Mockito style used in the project.
- Test names should be descriptive and follow the existing convention, for example:
  - `run_shouldThrowWhenRoleUserMissing`
  - `run_shouldPassWhenRoleUserExists`
  - `findById_shouldReturnUserWhenExists`
  - `create_shouldThrowWhenEmailAlreadyExists`

## Style rules
- Use constructor injection.
- Prefer `final` for dependencies.
- Keep methods focused and readable.
- Prefer explicit, descriptive names.
- Avoid overengineering.
- Do not add comments for obvious code.
- Do not introduce a new framework or library without a strong reason.
- Preserve current naming conventions across DTOs, exceptions, services, and controllers.

## API consistency rules
- Keep endpoint behavior consistent with existing controllers.
- Preserve response DTO patterns.
- Maintain coherent naming for request and response DTOs.
- Avoid mixing entity exposure directly into controller responses unless the project already does so in that area.

## Files and directories to treat carefully
- `src/main/resources/db/migration` — do not modify old migrations unless explicitly requested
- `com.company.shop.security` — changes here can affect the whole application
- `common.exception.GlobalExceptionHandler` — keep response format consistent
- `common.model` base entities — changes here affect many modules
- payment and Stripe-related order logic — validate carefully before changing

## Generated/build output rules
- Do not modify files under:
  - `target/`
  - generated sources
  - compiled classes
  - surefire reports
- Only modify source files under `src/` and top-level project files when needed.

## Commands for verification
Use the Maven wrapper if available; otherwise use Maven directly.

Typical validation commands:
- `mvn clean test`
- `mvn test`
- `mvn -q -DskipTests compile`

If the task touches configuration or packaging, also consider:
- `mvn clean package`

## Before finishing any task
Always check:
1. Is the code placed in the correct module and package?
2. Is the solution consistent with the existing architecture?
3. Are exceptions handled in the current project style?
4. Are DTOs, mappers, and services separated correctly?
5. Are tests added or updated for non-trivial changes?
6. Are imports, names, and package declarations correct?
7. Were old migrations left unchanged?
8. Were generated files and `target/` left untouched?

## Important constraints
- Do not perform broad refactors unless explicitly requested.
- Do not rename public APIs without a clear reason.
- Do not change database schema casually.
- Do not bypass security for convenience.
- Do not replace existing patterns just because an alternative is possible.
- Favor minimal, safe, reviewable changes.