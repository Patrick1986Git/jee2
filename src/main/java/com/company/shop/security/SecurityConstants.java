package com.company.shop.security;

public final class SecurityConstants {

    // Role
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    // HTTP Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    
    // Endpointy
    public static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/auth/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/swagger-ui/**",
            "/v3/api-docs/**" 
    };

    private SecurityConstants() {
        // Blokuje tworzenie instancji
        throw new UnsupportedOperationException("To jest klasa stałych i nie może być instancjonowana.");
    }
}