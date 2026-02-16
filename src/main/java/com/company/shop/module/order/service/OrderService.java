package com.company.shop.module.order.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.company.shop.module.order.dto.OrderCheckoutRequestDTO;
import com.company.shop.module.order.dto.OrderDetailedResponseDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;

public interface OrderService {
	OrderResponseDTO placeOrderFromCart(OrderCheckoutRequestDTO request);

	OrderDetailedResponseDTO findById(UUID id);

	Page<OrderResponseDTO> findAll(Pageable pageable);

	Page<OrderResponseDTO> findMyOrders(Pageable pageable);
}