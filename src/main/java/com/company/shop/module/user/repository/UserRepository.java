package com.company.shop.module.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.shop.module.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
	Optional<User> findByEmailWithRoles(@Param("email") String email);

	@Override
	@EntityGraph(attributePaths = "roles")
	Page<User> findAll(Pageable pageable);

	@EntityGraph(attributePaths = "roles")
	Optional<User> findWithRolesById(UUID id);
}
