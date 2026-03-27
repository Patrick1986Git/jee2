package com.company.shop.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.module.category.entity.Category;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.entity.ProductReview;
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PostgresContainerSupport;

import jakarta.persistence.PersistenceException;

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

        entityManager.persist(new ProductReview(product, user, 5, "Great"));
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> {
            entityManager.persist(new ProductReview(product, user, 4, "Still good"));
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class)
                .hasRootCauseInstanceOf(ConstraintViolationException.class);
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
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class);
    }

    private User persistUser(String email) {
        User user = new User(email, "encoded-pass", "First", "Last");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Product persistProduct(String name, String slug, String sku) {
        Category category = new Category("Electronics-" + UUID.randomUUID(), "electronics-" + UUID.randomUUID(), "desc");
        entityManager.persist(category);

        Product product = new Product(name, slug, sku, "desc", BigDecimal.valueOf(199.99), 10, category);
        entityManager.persist(product);
        entityManager.flush();
        return product;
    }
}
