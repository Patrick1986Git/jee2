package com.company.shop.module.cart.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class InsufficientStockException extends BusinessException {

	public InsufficientStockException(int available) {
		super(HttpStatus.CONFLICT, "Insufficient stock. Available: " + available, "INSUFFICIENT_STOCK");
	}
}