package com.company.shop.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.common.model.AuditableEntity;
import com.company.shop.module.category.entity.Category;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.entity.ProductReview;
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PostgresContainerSupport;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductReviewConstraintIT extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void persist_shouldThrowWhenSecondReviewForSameProductAndUserExists() {
        User user = persistUser("anna.nowak@example.com");
        Product product = persistProduct("Phone X", "phone-x", "SKU-PHONE-X");

        ProductReview firstReview = new ProductReview(product, user, 5, "Great");
        setCreatedAt(firstReview);
        entityManager.persist(firstReview);
        entityManager.flush();
        entityManager.clear();

        Product managedProduct = entityManager.getEntityManager().getReference(Product.class, product.getId());
        User managedUser = entityManager.getEntityManager().getReference(User.class, user.getId());

        ProductReview duplicateReview = new ProductReview(managedProduct, managedUser, 4, "Still good");
        setCreatedAt(duplicateReview);

        assertThatThrownBy(() -> {
            entityManager.persist(duplicateReview);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class)
                .hasRootCauseInstanceOf(PSQLException.class);
    }

    @Test
    void persist_shouldThrowWhenRatingOutsideAllowedRange() {
        User user = persistUser("marta.kowalska@example.com");
        Product product = persistProduct("Tablet Z", "tablet-z", "SKU-TABLET-Z");

        UUID reviewId = UUID.randomUUID();

        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                        INSERT INTO product_reviews (
                            id, product_id, user_id, rating, comment, created_at, deleted, version
                        ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, false, 0)
                        """,
                reviewId,
                product.getId(),
                user.getId(),
                6,
                "Invalid rating"
        )).isInstanceOf(DataIntegrityViolationException.class)
                .hasRootCauseInstanceOf(PSQLException.class);
    }

    private User persistUser(String email) {
        User user = new User(email, "encoded-pass", "First", "Last");
        setCreatedAt(user);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Product persistProduct(String name, String slug, String sku) {
        Category category = new Category(
                "Electronics-" + UUID.randomUUID(),
                "electronics-" + UUID.randomUUID(),
                "desc"
        );
        setCreatedAt(category);
        entityManager.persist(category);

        Product product = new Product(
                name,
                slug,
                sku,
                "desc",
                BigDecimal.valueOf(199.99),
                10,
                category
        );
        setCreatedAt(product);
        entityManager.persist(product);
        entityManager.flush();
        return product;
    }

    private void setCreatedAt(Object entity) {
        try {
            Field field = AuditableEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(entity, LocalDateTime.now());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set createdAt for test entity", e);
        }
    }
}