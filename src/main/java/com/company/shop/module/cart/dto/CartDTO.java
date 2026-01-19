package com.company.shop.module.cart.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CartDTO {

	private final List<CartItemDTO> items;

	public CartDTO(List<CartItemDTO> items) {
		this.items = items != null ? items : Collections.emptyList();
	}

	public List<CartItemDTO> getItems() {
		return items;
	}

	public BigDecimal getTotalAmount() {
		return items.stream().map(CartItemDTO::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public int getTotalItems() {
		return items.stream().mapToInt(CartItemDTO::getQuantity).sum();
	}
}