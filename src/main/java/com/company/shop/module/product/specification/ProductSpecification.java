/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.product.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.company.shop.module.category.entity.Category_;
import com.company.shop.module.product.dto.ProductSearchCriteria;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.entity.Product_;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

/**
 * Specification builder for dynamic filtering of {@link Product} entities.
 * <p>
 * This utility class leverages the JPA Criteria API and generated static metamodels
 * to provide type-safe query construction. It supports complex filtering including:
 * <ul>
 * <li>PostgreSQL Full-Text Search (FTS) integration</li>
 * <li>Category-based filtering with eager fetching optimization</li>
 * <li>Price range filtering</li>
 * <li>Rating threshold filtering</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
public class ProductSpecification {

    /**
     * Builds a dynamic JPA {@link Specification} based on the provided search criteria.
     * <p>
     * Note: Full-Text Search uses the custom SQL function {@code fts}, which must be 
     * registered in the database dialect or mapped to PostgreSQL's {@code @@} operator.
     * </p>
     *
     * @param criteria the search and filtering parameters provided by the client.
     * @return a {@link Specification} containing the combined predicates.
     */
    public static Specification<Product> filterByCriteria(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Optimization: Apply fetch join only for data queries, not for count queries (pagination)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch(Product_.CATEGORY, JoinType.LEFT);
            }

            // Full-Text Search implementation using the 'search_vector' column
            if (criteria.query() != null && !criteria.query().isBlank()) {
                // Convert search string to PostgreSQL tsquery format (AND-based)
                String tsQuery = criteria.query().trim().replaceAll("\\s+", " & ");

                predicates.add(
                        cb.isTrue(cb.function("fts", Boolean.class, root.get("search_vector"), cb.literal(tsQuery))));
            }

            // Category filter
            if (criteria.categoryId() != null) {
                predicates.add(cb.equal(root.get(Product_.CATEGORY).get(Category_.ID), criteria.categoryId()));
            }

            // Price range filters
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(Product_.PRICE), criteria.minPrice()));
            }

            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(Product_.PRICE), criteria.maxPrice()));
            }

            // Customer rating filter
            if (criteria.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(Product_.AVERAGE_RATING), criteria.minRating()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}