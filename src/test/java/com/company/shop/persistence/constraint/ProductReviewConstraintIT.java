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

import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.entity.ProductReview;
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PersistenceFixtures;
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
        User user = PersistenceFixtures.persistUser(entityManager, "anna.nowak@example.com");
        Product product = PersistenceFixtures.persistProduct(entityManager, "Phone X", "phone-x", "SKU-PHONE-X",
                BigDecimal.valueOf(199.99), 10);

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
        User user = PersistenceFixtures.persistUser(entityManager, "marta.kowalska@example.com");
        Product product = PersistenceFixtures.persistProduct(entityManager, "Tablet Z", "tablet-z", "SKU-TABLET-Z",
                BigDecimal.valueOf(199.99), 10);

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
}
