/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.category.service;

import java.util.UUID;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.common.exception.DuplicateResourceException;
import com.company.shop.module.category.dto.CategoryCreateDTO;
import com.company.shop.module.category.dto.CategoryResponseDTO;
import com.company.shop.module.category.entity.Category;
import com.company.shop.module.category.mapper.CategoryMapper;
import com.company.shop.module.category.repository.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Production implementation of {@link CategoryService} providing category lifecycle management.
 * <p>
 * This service handles hierarchical category structures, SEO-friendly slug generation
 * with Polish character normalization, and enforces unique category name constraints.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Constructs the service with required dependencies.
     *
     * @param categoryRepository repository for category persistence.
     * @param categoryMapper     mapper for DTO transformation.
     */
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with slug: " + slug));
    }

    /**
     * Creates a new category and optionally links it to a parent category.
     * <p>
     * Generates an SEO-friendly slug from the category name and enforces
     * uniqueness of category names.
     * </p>
     *
     * @param dto the category creation data.
     * @return the persisted category details.
     * @throws DuplicateResourceException if a category with the given name already exists.
     * @throws EntityNotFoundException    if the specified parent category does not exist.
     */
    @Override
    public CategoryResponseDTO create(CategoryCreateDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Category with name '" + dto.getName() + "' already exists");
        }

        String slug = generateSlug(dto.getName());

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with ID: " + dto.getParentId()));
        }

        Category category = new Category(dto.getName(), slug, dto.getDescription(), parent);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    /**
     * Updates an existing category's metadata and optional parent link.
     *
     * @param id  the identifier of the category to update.
     * @param dto the new category data.
     * @return the updated category details.
     * @throws EntityNotFoundException if the category or specified parent does not exist.
     */
    @Override
    public CategoryResponseDTO update(UUID id, CategoryCreateDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with ID: " + dto.getParentId()));
        }

        String newSlug = generateSlug(dto.getName());
        category.update(dto.getName(), newSlug, dto.getDescription(), parent);

        return categoryMapper.toDto(category);
    }

    @Override
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
        category.delete();
    }

    /**
     * Transforms a raw string into an SEO-compliant URL slug.
     * <p>
     * Example: "Telefony i Akcesoria" → "telefony-i-akcesoria"
     * </p>
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
