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
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.module.cart.entity.Cart;
import com.company.shop.module.cart.entity.CartItem;
import com.company.shop.module.category.entity.Category;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.user.entity.User;
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
        User user = persistUser("cart.user@example.com");
        Cart cart = persistCart(user);
        Product product = persistProduct("Headphones", "headphones", "SKU-HEADPHONES");

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
        User user = persistUser("cart.check@example.com");
        Cart cart = persistCart(user);
        Product product = persistProduct("Mouse", "mouse", "SKU-MOUSE");

        assertThatThrownBy(() -> {
            entityManager.persist(new CartItem(cart, product, 0));
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class)
                .hasRootCauseInstanceOf(ConstraintViolationException.class);
    }

    private User persistUser(String email) {
        User user = new User(email, "encoded-pass", "Cart", "User");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Cart persistCart(User user) {
        Cart cart = new Cart(user);
        entityManager.persist(cart);
        entityManager.flush();
        return cart;
    }

    private Product persistProduct(String name, String slugBase, String skuBase) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Category category = new Category("Accessories-" + suffix, "accessories-" + suffix, "desc");
        entityManager.persist(category);

        Product product = new Product(
                name,
                slugBase + "-" + suffix,
                skuBase + "-" + suffix,
                "desc",
                BigDecimal.valueOf(99.99),
                20,
                category);
        entityManager.persist(product);
        entityManager.flush();
        return product;
    }
}
