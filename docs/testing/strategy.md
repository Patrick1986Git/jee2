# Testing strategy (current state)

## What exists today
The repository currently emphasizes:

1. **Service-level unit tests** with Mockito and JUnit 5
   - Examples: cart/category/product/user/order/payment services.
2. **Security and contract-focused WebMvc tests**
   - Security filter-chain behavior and auth endpoint behavior.
   - Global exception contract serialization/shape checks.
3. **Repository-level persistence tests (`@DataJpaTest`)**
   - Includes repository behavior and persistence constraints coverage.
4. **Persistence integration tests with PostgreSQL Testcontainers support**
   - Shared PostgreSQL container support is used for persistence integration scenarios.
5. **Validation-focused and domain invariant tests**
   - DTO/custom validator coverage and product domain validation tests.

## Frameworks/patterns in use
- JUnit 5
- Mockito (`@ExtendWith(MockitoExtension.class)`)
- AssertJ
- Spring MVC slice tests (`@WebMvcTest`)
- Spring Security test support

Mockito static mocking is used in service tests (for example Stripe SDK static entry points), so the build config starts tests with Mockito as an explicit `-javaagent` to avoid JDK 21+ dynamic self-attach warnings and keep test execution compatible with stricter future JDK defaults.

## Integration coverage snapshot
- No full `@SpringBootTest` integration suite in `src/test/java`.
- Repository-level `@DataJpaTest` integration tests are present.
- Persistence integration tests run against PostgreSQL via shared Testcontainers support.
- Dedicated migration verification tests are present for Flyway smoke checks and schema-hardening migrations (`V13`, `V14`, `V15`).
- Stripe webhook flow is covered both at controller contract level (`@WebMvcTest`) and as persistence integration (`StripeWebhookPersistenceIT`).

## Practical test command set
Use Maven directly (no Maven wrapper in repo):

```bash
mvn test
mvn -q -DskipTests compile
```

Use `mvn clean test` when validating broad doc/code updates before release prep.

## Recommendation for next incremental improvement
Given current coverage, the next incremental step should target one additional end-to-end business-critical path (for example a checkout unhappy-path scenario spanning order creation + payment failure handling) while keeping tests focused and reviewable.
