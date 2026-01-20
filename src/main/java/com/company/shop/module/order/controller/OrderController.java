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

	/**
	 * Składanie zamówienia. Dostępne dla każdego zalogowanego użytkownika.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public OrderResponseDTO placeOrder(@Valid @RequestBody OrderCreateRequestDTO request) {
		return orderService.placeOrder(request);
	}

	/**
	 * Pobieranie szczegółów konkretnego zamówienia. Logika wewnątrz serwisu powinna
	 * sprawdzać, czy to zamówienie należy do usera lub czy user to ADMIN.
	 */
	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public OrderResponseDTO getOrderById(@PathVariable UUID id) {
		// Metoda do dodania w serwisie: return orderService.findById(id);
		return null;
	}

	/**
	 * Endpoint dla administratora do przeglądania wszystkich zamówień w systemie.
	 */
	@GetMapping("/admin/all")
	@PreAuthorize("hasRole('ADMIN')")
	public Page<OrderResponseDTO> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
		// Metoda do dodania w serwisie: return orderService.findAll(pageable);
		return null;
	}
}