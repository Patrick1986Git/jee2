/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.category.entity.Category;
import com.company.shop.module.category.repository.CategoryRepository;
import com.company.shop.module.product.dto.ProductCreateDTO;
import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.dto.ProductSearchCriteria;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.mapper.ProductMapper;
import com.company.shop.module.product.repository.ProductRepository;
import com.company.shop.module.product.specification.ProductSpecification;

import jakarta.persistence.EntityNotFoundException;

/**
 * Production implementation of {@link ProductService} providing high-level 
 * catalog management and search capabilities.
 * <p>
 * This service orchestrates complex business operations such as SEO-friendly slug 
 * generation, transactional media gallery updates, and advanced filtering 
 * through JPA Specifications.
 * </p>
 *
 * @since 1.0.0
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ProductMapper mapper;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Constructs the service with required core dependencies.
     */
    public ProductServiceImpl(ProductRepository productRepo, 
                              CategoryRepository categoryRepo, 
                              ProductMapper mapper) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepo.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAllByCategory(UUID categoryId, Pageable pageable) {
        return productRepo.findByCategoryId(categoryId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {
        return productRepo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findBySlug(String slug) {
        return productRepo.findBySlug(slug)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with slug: " + slug));
    }

    /**
     * Creates a new product and initializes its media gallery.
     * <p>
     * Ensures SKU uniqueness and handles potential slug collisions by 
     * appending random suffixes.
     * </p>
     *
     * @param dto the product creation data.
     * @return the persisted product details.
     * @throws IllegalArgumentException if SKU is already taken.
     */
    @Override
    public ProductResponseDTO create(ProductCreateDTO dto) {
        if (productRepo.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + dto.getSku() + " already exists");
        }

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        String slug = generateSlug(dto.getName());
        if (productRepo.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 5);
        }

        Product product = new Product(
                dto.getName(),
                slug,
                dto.getSku(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock(),
                category
        );
        
        // Populate media gallery via Aggregate Root
        product.replaceImages(dto.getImageUrls());

        return mapper.toDto(productRepo.save(product));
    }

    /**
     * Updates an existing product and synchronizes its image gallery.
     * <p>
     * Orchestrates the removal of orphaned images and the addition of new media 
     * within a single database transaction.
     * </p>
     *
     * @param id  the identifier of the product to update.
     * @param dto the new product state.
     * @return the updated product details.
     */
    @Override
    public ProductResponseDTO update(UUID id, ProductCreateDTO dto) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Update failed. Product not found."));

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        String newSlug = generateSlug(dto.getName());
        
        product.update(dto.getName(), newSlug, dto.getDescription(), dto.getPrice(), dto.getStock(), category);
        
        // Synchronize gallery state
        product.replaceImages(dto.getImageUrls());

        return mapper.toDto(productRepo.save(product));
    }

    @Override
    public void delete(UUID id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.delete();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        return productRepo.findAll(ProductSpecification.filterByCriteria(criteria), pageable)
                .map(mapper::toDto);
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