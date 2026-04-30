package com.company.shop.security.jwt;

import static com.company.shop.security.SecurityConstants.ROLE_ADMIN;
import static com.company.shop.security.SecurityConstants.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtTokenProviderTest {

    private static final String SECRET = "01234567890123456789012345678901";
    private static final String OTHER_SECRET = "abcdefghijklmnopqrstuvwxyzABCDEF";
    private static final String USERNAME = "john.doe@example.com";

    @Test
    void generateToken_shouldCreateValidTokenWithUsernameAndRoles() {
        JwtTokenProvider provider = tokenProvider(SECRET, 60_000L);
        Authentication authentication = authentication(USERNAME, ROLE_USER, ROLE_ADMIN);

        String token = provider.generateToken(authentication);
        String rolesClaim = provider.getRoles(token);

        assertThat(token).isNotBlank();
        assertThat(provider.validate(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo(USERNAME);
        assertThat(rolesClaim).isNotBlank();
        assertThat(rolesFrom(rolesClaim)).containsExactlyInAnyOrder(ROLE_USER, ROLE_ADMIN);
    }

    @Test
    void generateToken_shouldStoreSingleAuthorityAsRolesClaim() {
        JwtTokenProvider provider = tokenProvider(60_000L);
        Authentication authentication = authentication(USERNAME, ROLE_USER);

        String token = provider.generateToken(authentication);

        assertThat(provider.getRoles(token)).isEqualTo(ROLE_USER);
    }

    @Test
    void validate_shouldReturnFalseForMalformedToken() {
        JwtTokenProvider provider = tokenProvider(60_000L);

        assertThat(provider.validate("not-a-jwt-token")).isFalse();
    }

    @Test
    void validate_shouldReturnFalseForTokenSignedWithDifferentSecret() {
        JwtTokenProvider providerA = tokenProvider(SECRET, 60_000L);
        JwtTokenProvider providerB = tokenProvider(OTHER_SECRET, 60_000L);
        Authentication authentication = authentication(USERNAME, ROLE_USER, ROLE_ADMIN);

        String token = providerA.generateToken(authentication);

        assertThat(providerB.validate(token)).isFalse();
    }

    @Test
    void validate_shouldReturnFalseForExpiredToken() {
        JwtTokenProvider provider = tokenProvider(-1_000L);
        Authentication authentication = authentication(USERNAME, ROLE_USER);

        String token = provider.generateToken(authentication);

        assertThat(provider.validate(token)).isFalse();
    }

    @Test
    void validate_shouldReturnFalseForNullOrBlankToken() {
        JwtTokenProvider provider = tokenProvider(60_000L);

        assertThat(provider.validate(null)).isFalse();
        assertThat(provider.validate("")).isFalse();
        assertThat(provider.validate("   ")).isFalse();
    }

    private JwtTokenProvider tokenProvider(long expiration) {
        return tokenProvider(SECRET, expiration);
    }

    private JwtTokenProvider tokenProvider(String secret, long expiration) {
        JwtProperties properties = new JwtProperties(secret, expiration, 120_000L);
        return new JwtTokenProvider(properties);
    }

    private Authentication authentication(String username, String... roles) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "password",
                Arrays.stream(roles)
                        .map(SimpleGrantedAuthority::new)
                        .toList());
    }

    private Set<String> rolesFrom(String rolesClaim) {
        return Arrays.stream(rolesClaim.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toSet());
    }
}