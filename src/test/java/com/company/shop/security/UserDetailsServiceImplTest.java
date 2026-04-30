package com.company.shop.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.company.shop.module.user.entity.Role;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    private static final String RAW_EMAIL = "  USER@Example.COM  ";
    private static final String NORMALIZED_EMAIL = "user@example.com";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserDetailsServiceImpl(userRepository, new EmailNormalizer());
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithMappedRolesWhenUserExists() {
        User user = userWithRoles(SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN);
        when(userRepository.findActiveByEmailWithRoles(NORMALIZED_EMAIL)).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername(RAW_EMAIL);

        assertThat(result.getUsername()).isEqualTo(user.getEmail());
        assertThat(result.getPassword()).isEqualTo(user.getPassword());
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertAuthorities(result, SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN);

        verifyRepositoryLookedUpByNormalizedEmail();
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findActiveByEmailWithRoles(NORMALIZED_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername(RAW_EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verifyRepositoryLookedUpByNormalizedEmail();
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUserDetailsWhenUserDisabled() {
        User user = userWithRoles(SecurityConstants.ROLE_USER);
        user.disable();
        when(userRepository.findActiveByEmailWithRoles(NORMALIZED_EMAIL)).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername(RAW_EMAIL);

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertAuthorities(result, SecurityConstants.ROLE_USER);

        verifyRepositoryLookedUpByNormalizedEmail();
    }

    @Test
    void loadUserByUsername_shouldReturnLockedUserDetailsWhenUserDeleted() {
        User user = userWithRoles(SecurityConstants.ROLE_USER);
        user.delete();
        when(userRepository.findActiveByEmailWithRoles(NORMALIZED_EMAIL)).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername(RAW_EMAIL);

        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isFalse();
        assertAuthorities(result, SecurityConstants.ROLE_USER);

        verifyRepositoryLookedUpByNormalizedEmail();
    }

    @Test
    void loadUserByUsername_shouldRejectNullEmailBeforeRepositoryLookup() {
        assertThatThrownBy(() -> service.loadUserByUsername(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email must not be null");

        verifyNoInteractions(userRepository);
    }

    private User userWithRoles(String... roles) {
        User user = new User(NORMALIZED_EMAIL, ENCODED_PASSWORD, FIRST_NAME, LAST_NAME);
        for (String role : roles) {
            user.addRole(new Role(role));
        }
        return user;
    }

    private void assertAuthorities(UserDetails result, String... expectedAuthorities) {
        assertThat(result.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder(expectedAuthorities);
    }

    private void verifyRepositoryLookedUpByNormalizedEmail() {
        verify(userRepository).findActiveByEmailWithRoles(NORMALIZED_EMAIL);
        verifyNoMoreInteractions(userRepository);
    }
}