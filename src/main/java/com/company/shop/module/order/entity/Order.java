package com.company.shop.module.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.company.shop.common.model.SoftDeleteEntity;
import com.company.shop.module.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
@SQLRestriction("deleted = false")
public class Order extends SoftDeleteEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private OrderStatus status;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	protected Order() {
	}

	public Order(User user, BigDecimal totalAmount) {
		this.user = user;
		this.totalAmount = totalAmount;
		this.status = OrderStatus.NEW;
	}

	public void markAsPaid() {
		if (this.status != OrderStatus.NEW) {
			throw new IllegalStateException("Only NEW orders can be marked as PAID");
		}
		this.status = OrderStatus.PAID;
	}

	public void addItem(OrderItem item) {
		items.add(item);
		item.setOrder(this); // Kluczowe dla Hibernate!
	}

	public User getUser() {
		return user;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

}