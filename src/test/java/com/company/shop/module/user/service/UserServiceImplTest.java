package com.company.shop.module.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.shop.module.user.dto.UserUpdateDTO;
import com.company.shop.module.user.entity.Role;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.exception.UserNotFoundException;
import com.company.shop.module.user.mapper.UserMapper;
import com.company.shop.module.user.repository.UserRepository;
import com.company.shop.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, userMapper, currentUserProvider);
    }

    @Test
    void getCurrentUserEntity_shouldThrowWhenUserMissingInRepository() {
        String email = "john@example.com";
        when(currentUserProvider.getCurrentUserEmail()).thenReturn(email);
        when(userRepository.findActiveByEmailWithRoles(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUserEntity())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void update_shouldUseActiveLookupAndTrimNames() {
        UUID userId = UUID.randomUUID();
        User user = new User("john@example.com", "encoded", "John", "Doe");
        UserUpdateDTO dto = new UserUpdateDTO("  Jane ", " Doe-Smith  ");

        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));

        service.update(userId, dto);

        verify(userRepository).findActiveById(userId);
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Doe-Smith");
    }

    @Test
    void isAdmin_shouldReturnTrueWhenAdminRoleAssigned() {
        User user = new User("john@example.com", "encoded", "John", "Doe");

        Role admin = new Role("ROLE_ADMIN");
        Role basicUser = new Role("ROLE_USER");

        user.getRoles().addAll(Set.of(admin, basicUser));

        assertThat(service.isAdmin(user)).isTrue();
    }
}
