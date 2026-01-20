package com.company.shop.module.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.company.shop.module.order.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}