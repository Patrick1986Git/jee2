package com.company.shop.module.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.shop.module.order.entity.Payment;

import jakarta.persistence.LockModeType;

/**
 * Repository for {@link Payment} reads and writes.
 * <p>
 * Soft-deleted rows are filtered on entity level via {@code Payment @SQLRestriction("deleted = false")}.
 * </p>
 */
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

	/**
	 * Finds payment by order id.
	 *
	 * @param orderId order identifier
	 * @return payment for the order when visible by entity soft-delete policy
	 */
	java.util.Optional<Payment> findByOrderId(UUID orderId);

	/**
	 * Finds payment by order id with pessimistic write lock.
	 *
	 * @param orderId order identifier
	 * @return locked payment when visible by entity soft-delete policy
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
	java.util.Optional<Payment> findByOrderIdForUpdate(@Param("orderId") UUID orderId);
}
