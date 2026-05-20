package com.company.shop.common.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;

@SpringBootTest
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void getMessage_shouldResolvePolishMessageWhenLocaleIsPolish() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("pl"));

        final String message = messageService.getMessage("validation.notBlank");

        assertThat(message).isEqualTo("Pole nie mo\u017Ce by\u0107 puste");
    }

    @Test
    void getMessage_shouldResolveEnglishMessageWhenLocaleIsEnglish() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        final String message = messageService.getMessage("validation.notBlank");

        assertThat(message).isEqualTo("Field must not be empty");
    }

    @Test
    void getMessage_shouldThrowControlledExceptionWhenKeyMissing() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        assertThatThrownBy(() -> messageService.getMessage("missing.key.example"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing i18n message key: missing.key.example");
    }
}
