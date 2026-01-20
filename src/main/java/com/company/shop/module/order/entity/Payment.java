package com.company.shop.module.order.entity;

import java.math.BigDecimal;

import com.company.shop.common.model.SoftDeleteEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment extends SoftDeleteEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(name = "payment_method", nullable = false)
	private String provider;

	@Column(nullable = false)
	private BigDecimal amount;

	protected Payment() {
	}

	public Payment(Order order, String provider, BigDecimal amount) {
		this.order = order;
		this.provider = provider;
		this.amount = amount;
		this.status = PaymentStatus.PENDING;
	}
}