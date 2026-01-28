package com.company.shop.module.product.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductReviewRequestDTO(
    @NotNull UUID productId,
    @Min(1) @Max(5) int rating,
    @Size(max = 1000) String comment
) {}