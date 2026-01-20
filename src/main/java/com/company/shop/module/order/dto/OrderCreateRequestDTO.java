package com.company.shop.module.order.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public class OrderCreateRequestDTO {
	@NotEmpty
	private List<OrderItemRequestDTO> items;

	public List<OrderItemRequestDTO> getItems() {
		return items;
	}

	public static class OrderItemRequestDTO {
		private UUID productId;
		private int quantity;

		public UUID getProductId() {
			return productId;
		}

		public int getQuantity() {
			return quantity;
		}
	}
}