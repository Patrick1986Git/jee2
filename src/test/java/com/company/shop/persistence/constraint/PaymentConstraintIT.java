package com.company.shop.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.hibernate.exception.ConstraintViolationException;
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

import jakarta.persistence.PersistenceException;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PaymentConstraintIT extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persist_shouldThrowWhenSecondPaymentReferencesSameOrder() {
        User user = PersistenceFixtures.persistUser(entityManager, "payment.user@example.com");
        Order order = PersistenceFixtures.persistOrder(entityManager, user);

        entityManager.persist(new Payment(order, "STRIPE", BigDecimal.valueOf(150.00)));
        entityManager.flush();
        entityManager.clear();

        Order managedOrder = entityManager.getEntityManager().getReference(Order.class, order.getId());

        assertThatThrownBy(() -> {
            entityManager.persist(new Payment(managedOrder, "STRIPE", BigDecimal.valueOf(150.00)));
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class)
                .hasRootCauseInstanceOf(ConstraintViolationException.class);
    }
}
