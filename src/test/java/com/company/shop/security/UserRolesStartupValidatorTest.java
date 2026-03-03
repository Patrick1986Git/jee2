package com.company.shop.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.shop.module.user.exception.UserRoleNotConfiguredException;
import com.company.shop.module.user.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class UserRolesStartupValidatorTest {

    @Mock
    private RoleRepository roleRepository;

    @Test
    void run_shouldThrowWhenRoleUserMissing() {
        when(roleRepository.existsByName(SecurityConstants.ROLE_USER)).thenReturn(false);

        UserRolesStartupValidator validator = new UserRolesStartupValidator(roleRepository);

        assertThatThrownBy(() -> validator.run(null))
                .isInstanceOf(UserRoleNotConfiguredException.class)
                .hasMessageContaining(SecurityConstants.ROLE_USER);
    }

    @Test
    void run_shouldPassWhenRoleUserExists() {
        when(roleRepository.existsByName(SecurityConstants.ROLE_USER)).thenReturn(true);

        UserRolesStartupValidator validator = new UserRolesStartupValidator(roleRepository);

        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }
}
