package com.company.shop.module.order.dto;

public record PaymentIntentResponseDTO(
    String clientSecret,
    String publishableKey
) {}