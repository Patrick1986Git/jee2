package com.company.shop.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.common.model.AuditableEntity;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.Payment;
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PostgresContainerSupport;

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

        Payment firstPayment = new Payment(order, "STRIPE", BigDecimal.valueOf(150.00));
        setCreatedAt(firstPayment, LocalDateTime.now());
        entityManager.persist(firstPayment);
        entityManager.flush();
        entityManager.clear();

        Order managedOrder = entityManager.getEntityManager().getReference(Order.class, order.getId());

        assertThatThrownBy(() -> {
            Payment secondPayment = new Payment(managedOrder, "STRIPE", BigDecimal.valueOf(150.00));
            setCreatedAt(secondPayment, LocalDateTime.now());
            entityManager.persist(secondPayment);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    private User persistUser(String email) {
        User user = new User(email, "encoded-pass", "Payment", "User");
        setCreatedAt(user, LocalDateTime.now());
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Order persistOrder(User user) {
        Order order = new Order(user);
        setCreatedAt(order, LocalDateTime.now());
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }

    private void setCreatedAt(Object entity, LocalDateTime createdAt) {
        try {
            Field field = AuditableEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(entity, createdAt);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set createdAt for test entity", e);
        }
    }
}