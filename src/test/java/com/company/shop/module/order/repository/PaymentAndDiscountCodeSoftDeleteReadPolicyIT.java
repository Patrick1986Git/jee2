package com.company.shop.module.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.Payment;
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PersistenceFixtures;
import com.company.shop.persistence.support.PostgresContainerSupport;

/**
 * Verifies repository read policy for soft-deleted {@link Payment} and {@link com.company.shop.module.order.entity.DiscountCode}.
 * <p>
 * Both repositories rely on entity-level {@code @SQLRestriction("deleted = false")}.
 * </p>
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PaymentAndDiscountCodeSoftDeleteReadPolicyIT extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @Test
    void paymentRepository_shouldReturnActivePaymentAndHideSoftDeletedPayment() {
        User user = PersistenceFixtures.persistUser(entityManager, "soft.payment@example.com");
        Order activeOrder = PersistenceFixtures.persistOrder(entityManager, user);
        Order deletedOrder = PersistenceFixtures.persistOrder(entityManager, user);

        PersistenceFixtures.persistPayment(entityManager, activeOrder, "STRIPE", BigDecimal.valueOf(50));

        Payment deletedPayment = new Payment(deletedOrder, "STRIPE", BigDecimal.valueOf(50));
        PersistenceFixtures.setCreatedAt(deletedPayment);
        deletedPayment.markDeleted();
        entityManager.persist(deletedPayment);
        entityManager.flush();
        entityManager.clear();

        assertThat(paymentRepository.findByOrderId(activeOrder.getId())).isPresent();
        assertThat(paymentRepository.findByOrderIdForUpdate(activeOrder.getId())).isPresent();

        assertThat(paymentRepository.findByOrderId(deletedOrder.getId())).isEmpty();
        assertThat(paymentRepository.findByOrderIdForUpdate(deletedOrder.getId())).isEmpty();
    }

    @Test
    void discountCodeRepository_shouldReturnActiveCodeAndHideSoftDeletedCode() {
        String activeCode = "ACTIVE10-" + UUID.randomUUID().toString().substring(0, 8);
        String deletedCode = "DELETED10-" + UUID.randomUUID().toString().substring(0, 8);

        PersistenceFixtures.insertDiscountCode(entityManager, activeCode, false);
        PersistenceFixtures.insertDiscountCode(entityManager, deletedCode, true);

        assertThat(discountCodeRepository.findByCodeIgnoreCase(activeCode.toLowerCase())).isPresent();
        assertThat(discountCodeRepository.findByCodeIgnoreCase(deletedCode.toLowerCase())).isEmpty();
    }
}
