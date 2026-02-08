/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.company.shop.module.product.dto.ProductCreateDTO;
import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.dto.ProductSearchCriteria;
import com.company.shop.module.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller providing endpoints for product catalog management.
 * <p>
 * This controller exposes APIs for public product browsing, searching, 
 * and administrative tasks such as creating or deleting products.
 * </p>
 */
@RestController
@RequestMapping("/products")
@Tag(name = "Product Controller", description = "Endpoints for managing and searching products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves a paginated list of all products.
     *
     * @param pageable pagination parameters (default size 12).
     * @return a page of product records.
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Returns a paginated list of all products in the catalog")
    public Page<ProductResponseDTO> getAll(@PageableDefault(size = 12) Pageable pageable) {
        return productService.findAll(pageable);
    }

    /**
     * Filters products by their category.
     *
     * @param categoryId the UUID of the category.
     * @param pageable   pagination parameters.
     * @return a page of products within the specified category.
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public Page<ProductResponseDTO> getByCategory(@PathVariable UUID categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        return productService.findAllByCategory(categoryId, pageable);
    }

    /**
     * Fetches a single product details by its SEO slug.
     *
     * @param slug the unique product slug.
     * @return the product data.
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Retrieve product details using its URL-friendly unique name")
    public ProductResponseDTO getBySlug(@PathVariable String slug) {
        return productService.findBySlug(slug);
    }

    /**
     * Admin-only: retrieve product details by ID.
     *
     * @param id product identifier.
     * @return the product data.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product by ID (Admin only)")
    public ProductResponseDTO getById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    /**
     * Admin-only: create a new product.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product (Admin only)")
    public ProductResponseDTO create(@Valid @RequestBody ProductCreateDTO dto) {
        return productService.create(dto);
    }

    /**
     * Admin-only: update product details.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product (Admin only)")
    public ProductResponseDTO update(@PathVariable UUID id, @Valid @RequestBody ProductCreateDTO dto) {
        return productService.update(id, dto);
    }

    /**
     * Admin-only: delete a product.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product (Admin only)")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }

    /**
     * Searches for products using advanced criteria (full-text search, price ranges, etc.).
     * <p>
     * Uses {@link ModelAttribute} to bind query parameters to the {@link ProductSearchCriteria} object.
     * </p>
     *
     * @param criteria the search filters.
     * @param pageable pagination and sorting.
     * @return a filtered page of products.
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Performs full-text search and applies filters like price and rating")
    public Page<ProductResponseDTO> search(
            @Valid @ModelAttribute ProductSearchCriteria criteria, 
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.searchProducts(criteria, pageable);
    }
}