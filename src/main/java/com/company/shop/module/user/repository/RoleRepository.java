package com.company.shop.module.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.shop.module.user.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByName(String name);
}
