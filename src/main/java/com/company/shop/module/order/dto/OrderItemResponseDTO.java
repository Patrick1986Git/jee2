package com.company.shop.module.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(
    UUID productId,
    String productName,
    String sku,
    int quantity,
    BigDecimal price,
    BigDecimal subtotal
) {}