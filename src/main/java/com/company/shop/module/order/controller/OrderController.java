package com.company.shop.module.order.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.order.dto.OrderCreateRequestDTO;
import com.company.shop.module.order.dto.OrderDetailedResponseDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;
import com.company.shop.module.order.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public OrderResponseDTO placeOrder(@Valid @RequestBody OrderCreateRequestDTO request) {
		return orderService.placeOrder(request);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public OrderDetailedResponseDTO getOrderById(@PathVariable UUID id) {
		return orderService.findById(id); // Wywołuje metodę z weryfikacją właściciela
	}

	@GetMapping("/my")
	@PreAuthorize("isAuthenticated()")
	public Page<OrderResponseDTO> getMyOrders(@PageableDefault(size = 10) Pageable pageable) {
		return orderService.findMyOrders(pageable);
	}

	@GetMapping("/admin/all")
	@PreAuthorize("hasRole('ADMIN')")
	public Page<OrderResponseDTO> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
		return orderService.findAll(pageable);
	}
}