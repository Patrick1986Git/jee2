# Testing strategy (current state)

## What exists today
The repository currently emphasizes:

1. **Service-level unit tests** with Mockito and JUnit 5
   - Examples: cart/category/product/user/order/payment services.
2. **Security and contract-focused WebMvc tests**
   - Security filter-chain behavior and auth endpoint behavior.
   - Global exception contract serialization/shape checks.
3. **Validation-focused tests**
   - DTO and custom validator tests.
4. **Domain invariant tests**
   - Product entity/domain validation tests.

## Frameworks/patterns in use
- JUnit 5
- Mockito (`@ExtendWith(MockitoExtension.class)`)
- AssertJ
- Spring MVC slice tests (`@WebMvcTest`)
- Spring Security test support

## Not present yet
- No full `@SpringBootTest` integration suite in `src/test/java`.
- No repository-level `@DataJpaTest` suite at the moment.
- Testcontainers dependency exists in `pom.xml`, but containerized integration tests are not currently part of the committed test set.

## Practical test command set
Use Maven directly (no Maven wrapper in repo):

```bash
mvn test
mvn -q -DskipTests compile
```

Use `mvn clean test` when validating broad doc/code updates before release prep.

## Recommendation for next incremental improvement
If one new integration test area is added, prioritize **Stripe webhook + persistence interaction** (order status + payment status + cart clear) because it is cross-module and business-critical.
