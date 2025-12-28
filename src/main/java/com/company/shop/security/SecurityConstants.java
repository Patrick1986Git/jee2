package com.company.shop.security;

public final class SecurityConstants {

    private SecurityConstants() {
    	
    }
    
    public static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/auth/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/swagger-ui/**",
			"/v3/api-docs/**" };
}
