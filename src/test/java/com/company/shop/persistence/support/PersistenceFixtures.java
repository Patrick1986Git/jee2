package com.company.shop.persistence.support;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.company.shop.module.cart.entity.Cart;
import com.company.shop.module.category.entity.Category;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.user.entity.User;

/**
 * Minimal fixture helpers for persistence integration tests.
 * <p>
 * Keeps tests concise without introducing a full builder framework.
 * </p>
 */
public final class PersistenceFixtures {

    private PersistenceFixtures() {
    }

    public static User persistUser(TestEntityManager entityManager, String email) {
        User user = new User(email, "encoded-pass", "Test", "User");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    public static Category persistCategory(TestEntityManager entityManager, String baseName) {
        String suffix = shortSuffix();
        Category category = new Category(baseName + "-" + suffix, baseName.toLowerCase(Locale.ROOT) + "-" + suffix, "desc");
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }

    public static Product persistProduct(TestEntityManager entityManager, String name, String slugBase, String skuBase,
            BigDecimal price, int stock) {
        Category category = persistCategory(entityManager, "category");
        return persistProduct(entityManager, name, slugBase, skuBase, price, stock, category);
    }

    public static Product persistProduct(TestEntityManager entityManager, String name, String slugBase, String skuBase,
            BigDecimal price, int stock, Category category) {
        String suffix = shortSuffix();

        Product product = new Product(
                name,
                slugBase + "-" + suffix,
                skuBase + "-" + suffix,
                "desc",
                price,
                stock,
                category);
        entityManager.persist(product);
        entityManager.flush();
        return product;
    }

    public static Cart persistCart(TestEntityManager entityManager, User user) {
        Cart cart = new Cart(user);
        entityManager.persist(cart);
        entityManager.flush();
        return cart;
    }

    public static Order persistOrder(TestEntityManager entityManager, User user) {
        Order order = new Order(user);
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }

    private static String shortSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
