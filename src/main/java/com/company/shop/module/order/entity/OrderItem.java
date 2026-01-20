package com.company.shop.module.order.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.company.shop.module.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id")
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	protected OrderItem() {
	}

	public OrderItem(Product product, int quantity, BigDecimal price) {
		this.product = product;
		this.quantity = quantity;
		this.price = price;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public UUID getId() {
		return id;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}
}