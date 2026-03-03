package com.company.shop.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailNormalizerTest {

    private final EmailNormalizer normalizer = new EmailNormalizer();

    @Test
    void normalize_shouldTrimAndLowercaseEmail() {
        assertThat(normalizer.normalize("  User@Example.COM ")).isEqualTo("user@example.com");
    }

    @Test
    void normalize_shouldThrowWhenEmailIsNull() {
        assertThatThrownBy(() -> normalizer.normalize(null))
                .isInstanceOf(IllegalArgumentException.class)
                 .hasMessageContaining("email");
    }
}
