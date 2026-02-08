/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

/**
 * Contributes custom SQL functions to the Hibernate function registry.
 * <p>
 * This class extends Hibernate's capabilities by registering database-specific functions 
 * that are not available out-of-the-box via Criteria API. Specifically, it enables 
 * <strong>PostgreSQL Full-Text Search</strong> using the {@code @@} operator.
 * </p>
 * * <p><strong>Usage in Criteria API:</strong></p>
 * {@code cb.function("fts", Boolean.class, root.get("search_vector"), cb.literal(tsQuery))}
 *
 * @since 1.0.0
 */
public class SqlFunctionsContributor implements FunctionContributor {

    /**
     * Registers custom functions into the Hibernate engine.
     * <p>
     * Function {@code fts}:
     * <ul>
     * <li><strong>Pattern:</strong> {@code ?1 @@ to_tsquery('polish', ?2)}</li>
     * <li><strong>Returns:</strong> Boolean</li>
     * <li><strong>Description:</strong> Maps to PostgreSQL text search matching operator.</li>
     * </ul>
     * </p>
     *
     * @param contributions the contribution object used to register functions and types.
     */
    @Override
    public void contributeFunctions(FunctionContributions contributions) {       
        contributions.getFunctionRegistry().registerPattern(
            "fts", 
            "?1 @@ to_tsquery('polish', ?2)",
            contributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN)
        );
    }
}