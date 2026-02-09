/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.company.shop.module.product.entity.Product;

/**
 * Repository interface for {@link Product} entity management.
 * <p>
 * This interface combines standard CRUD operations via {@link JpaRepository} 
 * with advanced, type-safe dynamic querying capabilities via {@link JpaSpecificationExecutor}.
 * It supports pagination, sorting, and SEO-friendly lookups.
 * </p>
 *
 * @since 1.0.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    /**
     * Retrieves a product by its unique SEO-friendly slug.
     *
     * @param slug the unique string identifier used in URLs.
     * @return an {@link Optional} containing the product if found.
     */
    Optional<Product> findBySlug(String slug);

    /**
     * Retrieves a product by its unique Stock Keeping Unit (SKU).
     *
     * @param sku the unique business identifier for the product.
     * @return an {@link Optional} containing the product if found.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Finds products belonging to a specific category with pagination support.
     *
     * @param categoryId unique identifier of the category.
     * @param pageable   pagination and sorting information.
     * @return a page of products matching the category.
     */
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    /**
     * Performs a basic case-insensitive search by product name.
     * <p>
     * Note: For advanced linguistic search, use Specifications with PostgreSQL FTS.
     * </p>
     *
     * @param name     the partial name to search for.
     * @param pageable pagination and sorting information.
     * @return a page of products containing the name string.
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Checks if a product with the given SKU already exists in the system.
     *
     * @param sku the SKU to check.
     * @return true if the SKU is already taken.
     */
    boolean existsBySku(String sku);

    /**
     * Checks if a product with the given slug already exists in the system.
     *
     * @param slug the slug to check.
     * @return true if the slug is already taken.
     */
    boolean existsBySlug(String slug);
}