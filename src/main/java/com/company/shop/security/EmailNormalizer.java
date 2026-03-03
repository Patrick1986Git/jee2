package com.company.shop.security;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class EmailNormalizer {

    public String normalize(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email must not be null");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
