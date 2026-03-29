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

import com.company.shop.module.cart.entity.Cart;
import com.company.shop.module.cart.entity.CartItem;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PersistenceFixtures;
import com.company.shop.persistence.support.PostgresContainerSupport;

import jakarta.persistence.PersistenceException;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CartItemConstraintIT extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persist_shouldThrowWhenDuplicateProductExistsInSameCart() {
        User user = PersistenceFixtures.persistUser(entityManager, "cart.user@example.com");
        Cart cart = PersistenceFixtures.persistCart(entityManager, user);
        Product product = PersistenceFixtures.persistProduct(entityManager, "Headphones", "headphones",
                "SKU-HEADPHONES", BigDecimal.valueOf(99.99), 20);

        entityManager.persist(new CartItem(cart, product, 1));
        entityManager.flush();
        entityManager.clear();

        Cart managedCart = entityManager.getEntityManager().getReference(Cart.class, cart.getId());
        Product managedProduct = entityManager.getEntityManager().getReference(Product.class, product.getId());

        assertThatThrownBy(() -> {
            entityManager.persist(new CartItem(managedCart, managedProduct, 2));
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class)
                .hasRootCauseInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void persist_shouldThrowWhenQuantityIsNotPositive() {
        User user = PersistenceFixtures.persistUser(entityManager, "cart.check@example.com");
        Cart cart = PersistenceFixtures.persistCart(entityManager, user);
        Product product = PersistenceFixtures.persistProduct(entityManager, "Mouse", "mouse", "SKU-MOUSE",
                BigDecimal.valueOf(99.99), 20);

        assertThatThrownBy(() -> {
            entityManager.persist(new CartItem(cart, product, 0));
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class)
                .hasRootCauseInstanceOf(ConstraintViolationException.class);
    }
}
