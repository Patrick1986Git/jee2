package com.company.shop.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.company.shop.module.user.exception.UserRoleNotConfiguredException;
import com.company.shop.module.user.repository.RoleRepository;

@Component
public class UserRolesStartupValidator implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public UserRolesStartupValidator(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!roleRepository.existsByName(SecurityConstants.ROLE_USER)) {
            throw new UserRoleNotConfiguredException(SecurityConstants.ROLE_USER);
        }
    }
}
