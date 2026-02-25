package com.company.shop.module.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.company.shop.module.order.entity.Payment;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
	java.util.Optional<Payment> findByOrderIdAndDeletedFalse(UUID orderId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.deleted = false")
	java.util.Optional<Payment> findByOrderIdForUpdate(@Param("orderId") UUID orderId);
}