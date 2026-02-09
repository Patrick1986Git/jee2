/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.module.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import com.company.shop.module.order.entity.DiscountCode;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

/**
 * Repository interface for {@link DiscountCode} entity access.
 * <p>
 * Provides standard CRUD operations and specialized methods for secure 
 * discount code retrieval with concurrency control.
 * </p>
 *
 * @since 1.0.0
 */
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, UUID> {

    /**
     * Retrieves an active discount code by its string representation, ignoring case.
     * <p>
     * <strong>Concurrency Control:</strong> This method applies a {@code PESSIMISTIC_WRITE} 
     * lock to prevent race conditions during the "check-then-update" cycle of the 
     * code's usage limit.
     * </p>
     *
     * @param code the unique discount code string (case-insensitive).
     * @return an {@link Optional} containing the discount code if found and not deleted.
     * @see LockModeType#PESSIMISTIC_WRITE
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") })
    Optional<DiscountCode> findByCodeIgnoreCaseAndDeletedFalse(String code);
}