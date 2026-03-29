package com.company.shop.persistence.constraint;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.company.shop.module.user.entity.User;
import com.company.shop.persistence.support.PostgresContainerSupport;

import jakarta.persistence.PersistenceException;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserEmailConstraintIT extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persist_shouldThrowWhenEmailDiffersOnlyByCase() {
        User firstUser = new User("john.doe@example.com", "encoded-pass", "John", "Doe");
        entityManager.persist(firstUser);
        entityManager.flush();

        User duplicateByCase = new User("JOHN.DOE@EXAMPLE.COM", "encoded-pass-2", "John", "Doe");

        assertThatThrownBy(() -> {
            entityManager.persist(duplicateByCase);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class)
                .hasRootCauseInstanceOf(ConstraintViolationException.class);
    }
}
