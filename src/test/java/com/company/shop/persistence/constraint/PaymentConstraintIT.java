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
        User user = persistUser("payment.user@example.com");
        Order order = persistOrder(user);

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

    private User persistUser(String email) {
        User user = new User(email, "encoded-pass", "Payment", "User");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Order persistOrder(User user) {
        Order order = new Order(user);
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }
}
