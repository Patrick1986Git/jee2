package com.company.shop.module.product.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductReviewResponseDTO(
    UUID id,
    String authorName,
    int rating,
    String comment,
    LocalDateTime createdAt
) {}