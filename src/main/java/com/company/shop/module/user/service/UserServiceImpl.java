/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.user.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.user.dto.UserResponseDTO;
import com.company.shop.module.user.dto.UserUpdateDTO;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.mapper.UserMapper;
import com.company.shop.module.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Production implementation of {@link UserService} providing user account management.
 * <p>
 * This service integrates with the Spring Security context to resolve the currently 
 * authenticated user and supports user lifecycle operations including profile updates 
 * and soft deletion.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Constructs the service with required dependencies.
     *
     * @param userRepository repository for user persistence.
     * @param userMapper     mapper for DTO transformation.
     */
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        return userRepository.findWithRolesById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * <p>
     * Resolves the email (principal name) from the {@link SecurityContextHolder}
     * set by the JWT authentication filter.
     * </p>
     *
     * @return the current user's profile DTO.
     * @throws EntityNotFoundException if the authenticated user's account is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailWithRoles(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for authenticated user: " + email));
    }

    @Override
    public UserResponseDTO update(UUID id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot update. User not found with ID: " + id));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        // Dirty checking within the active transaction automatically persists changes;
        // the explicit return relies on the managed entity state.
        return userMapper.toDto(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot delete. User not found with ID: " + id));
        user.markDeleted();
    }

    /**
     * Retrieves the raw {@link User} entity for the currently authenticated principal.
     * <p>
     * Used internally by other services that require the full entity (e.g., cart, order).
     * </p>
     *
     * @return the authenticated user entity with roles eagerly loaded.
     * @throws EntityNotFoundException if the authenticated user is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + email));
    }
}
