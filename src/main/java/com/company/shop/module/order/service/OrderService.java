package com.company.shop.module.order.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.company.shop.module.order.dto.OrderCreateRequestDTO;
import com.company.shop.module.order.dto.OrderDetailedResponseDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;

public interface OrderService {
	OrderResponseDTO placeOrder(OrderCreateRequestDTO request);

	OrderDetailedResponseDTO findById(UUID id);
	
	Page<OrderResponseDTO> findAll(Pageable pageable); // Metoda dla admina (widzi wszystko)

	Page<OrderResponseDTO> findMyOrders(Pageable pageable); // Opcjonalnie: lista zamówień tylko dla zalogowanego usera
}