package com.company.shop.module.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.company.shop.module.order.entity.OrderStatus;

public record OrderResponseDTO(UUID id, OrderStatus status, BigDecimal totalAmount, LocalDateTime createdAt) {
}