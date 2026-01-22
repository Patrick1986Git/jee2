package com.company.shop.module.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.company.shop.module.order.entity.OrderStatus;

public record OrderDetailedResponseDTO(
    UUID id,
    OrderStatus status,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    String userEmail,
    List<OrderItemResponseDTO> items
) {}