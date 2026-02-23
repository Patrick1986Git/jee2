/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.category.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.category.dto.CategoryCreateDTO;
import com.company.shop.module.category.dto.CategoryResponseDTO;
import com.company.shop.module.category.entity.Category;
import com.company.shop.module.category.mapper.CategoryMapper;
import com.company.shop.module.category.repository.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Production implementation of {@link CategoryService} providing category catalog management.
 * <p>
 * This service handles category lifecycle operations including hierarchical parent-child
 * relationships and SEO-friendly slug generation. All write operations are executed
 * within a transaction to ensure data consistency.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Constructs the service with required dependencies.
     *
     * @param repo   repository for category persistence.
     * @param mapper mapper for DTO transformation.
     */
    public CategoryServiceImpl(CategoryRepository repo, CategoryMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(UUID id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findBySlug(String slug) {
        return repo.findBySlug(slug)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with slug: " + slug));
    }

    /**
     * Creates a new category with an auto-generated slug.
     * <p>
     * Validates uniqueness of the category name and resolves the optional parent category.
     * </p>
     *
     * @param dto the category creation data.
     * @return the persisted category details.
     * @throws IllegalArgumentException if a category with the same name already exists.
     * @throws EntityNotFoundException  if the specified parent category does not exist.
     */
    @Override
    public CategoryResponseDTO create(CategoryCreateDTO dto) {
        if (repo.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        String slug = generateSlug(dto.getName());

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = repo.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with ID: " + dto.getParentId()));
        }

        Category category = new Category(dto.getName(), slug, dto.getDescription(), parent);
        return mapper.toDto(repo.save(category));
    }

    /**
     * Updates an existing category's fields and regenerates its slug.
     *
     * @param id  the identifier of the category to update.
     * @param dto the new category state.
     * @return the updated category details.
     * @throws EntityNotFoundException if the category or specified parent category does not exist.
     */
    @Override
    public CategoryResponseDTO update(UUID id, CategoryCreateDTO dto) {
        Category category = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = repo.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with ID: " + dto.getParentId()));
        }

        String newSlug = generateSlug(dto.getName());
        category.update(dto.getName(), newSlug, dto.getDescription(), parent);

        return mapper.toDto(category);
    }

    @Override
    public void delete(UUID id) {
        Category category = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
        category.delete();
    }

    /**
     * Transforms a raw string into an SEO-compliant URL slug.
     *
     * @param input the string to slugify.
     * @return a normalized, lowercase, hyphenated string.
     */
    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}