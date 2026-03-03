package com.company.shop.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.company.shop.module.user.exception.UserAuthenticationRequiredException;

class SecurityCurrentUserProviderTest {

    private final SecurityCurrentUserProvider provider = new SecurityCurrentUserProvider(new EmailNormalizer());

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserEmail_shouldThrowForAnonymousToken() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThatThrownBy(provider::getCurrentUserEmail)
                .isInstanceOf(UserAuthenticationRequiredException.class);
    }

    @Test
    void getCurrentUserEmail_shouldThrowWhenAuthenticationIsNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(provider::getCurrentUserEmail)
                .isInstanceOf(UserAuthenticationRequiredException.class);
    }

    @Test
    void getCurrentUserEmail_shouldReturnNormalizedPrincipalNameForAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("  John@Example.com ", "secret"));

        assertThat(provider.getCurrentUserEmail()).isEqualTo("john@example.com");
    }
}
