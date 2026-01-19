package com.company.shop.module.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class CartItemDTO {

	private final UUID productId;
	private final String sku;
	private final String productName;
	private final BigDecimal price;
	private final int quantity;

	public CartItemDTO(UUID productId, String sku, String productName, BigDecimal price, int quantity) {
		this.productId = productId;
		this.sku = sku;
		this.productName = productName;
		this.price = price;
		this.quantity = quantity;
	}

	public UUID getProductId() {
		return productId;
	}

	public String getSku() {
		return sku;
	}

	public String getProductName() {
		return productName;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getTotal() {
		return price.multiply(BigDecimal.valueOf(quantity));
	}
}