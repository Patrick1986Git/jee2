package com.company.shop.module.order.service;

import com.company.shop.module.order.dto.OrderCreateRequestDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;

public interface OrderService {
	OrderResponseDTO placeOrder(OrderCreateRequestDTO request);
}