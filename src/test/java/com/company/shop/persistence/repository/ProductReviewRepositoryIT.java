package com.company.shop.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.entity.ProductReview;
import com.company.shop.module.product.repository.ProductReviewRepository;
import com.company.shop.module.product.repository.RatingStats;
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PersistenceFixtures;
import com.company.shop.persistence.support.PostgresContainerSupport;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductReviewRepositoryIT extends PostgresContainerSupport {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void getRatingStatsByProductId_shouldCalculateAverageAndCountUsingOnlyActiveReviews() {
        Product product = PersistenceFixtures.persistProduct(entityManager, "Monitor", "monitor", "SKU-MONITOR",
                BigDecimal.valueOf(1499L), 12);

        User firstUser = PersistenceFixtures.persistUser(entityManager, uniqueEmail("review.stats"));
        User secondUser = PersistenceFixtures.persistUser(entityManager, uniqueEmail("review.stats"));
        User thirdUser = PersistenceFixtures.persistUser(entityManager, uniqueEmail("review.stats"));

        ProductReview firstReview = new ProductReview(product, firstUser, 5, "Excellent");
        ProductReview secondReview = new ProductReview(product, secondUser, 3, "Okay");
        ProductReview deletedReview = new ProductReview(product, thirdUser, 1, "Outdated opinion");
        deletedReview.markDeleted();

        entityManager.persist(firstReview);
        entityManager.persist(secondReview);
        entityManager.persist(deletedReview);
        entityManager.flush();
        entityManager.clear();

        RatingStats stats = productReviewRepository.getRatingStatsByProductId(product.getId());

        assertThat(stats).isNotNull();
        assertThat(stats.reviewCount()).isEqualTo(2L);
        assertThat(stats.averageRating()).isEqualTo(4.0d);
    }

    @Test
    void findByProductId_shouldReturnOnlyNotDeletedReviews() {
        Product product = PersistenceFixtures.persistProduct(entityManager, "Keyboard", "keyboard", "SKU-KEYBOARD",
                BigDecimal.valueOf(299L), 40);

        User activeUser = PersistenceFixtures.persistUser(entityManager, uniqueEmail("review.find"));
        User deletedUser = PersistenceFixtures.persistUser(entityManager, uniqueEmail("review.find"));

        ProductReview activeReview = new ProductReview(product, activeUser, 4, "Solid");
        ProductReview softDeletedReview = new ProductReview(product, deletedUser, 2, "Not for me");
        softDeletedReview.markDeleted();

        entityManager.persist(activeReview);
        entityManager.persist(softDeletedReview);
        entityManager.flush();
        entityManager.clear();

        var page = productReviewRepository.findByProductId(product.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getUser().getId()).isEqualTo(activeUser.getId());
        assertThat(page.getContent().get(0).isDeleted()).isFalse();
    }

    private String uniqueEmail(String base) {
        return base + "." + UUID.randomUUID().toString().replace("-", "") + "@example.com";
    }
}
