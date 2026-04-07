package com.company.shop.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
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
import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PostgresContainerSupport;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserEmailConstraintIT extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persist_shouldThrowWhenEmailDiffersOnlyByCase() {
        User firstUser = buildUser("john.doe@example.com", "encoded-pass");
        entityManager.persist(firstUser);
        entityManager.flush();

        User duplicateByCase = buildUser("JOHN.DOE@EXAMPLE.COM", "encoded-pass-2");

        assertThatThrownBy(() -> {
            entityManager.persist(duplicateByCase);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("ux_users_email_lower");
    }

    private User buildUser(String email, String password) {
        User user = new User(email, password, "John", "Doe");
        setCreatedAt(user, LocalDateTime.now());
        return user;
    }

    private void setCreatedAt(User user, LocalDateTime createdAt) {
        try {
            Field field = AuditableEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(user, createdAt);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set createdAt for test entity", e);
        }
    }
}