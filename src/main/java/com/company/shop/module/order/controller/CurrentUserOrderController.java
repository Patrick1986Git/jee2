package com.company.shop.module.order.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.order.dto.OrderCheckoutRequestDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;
import com.company.shop.module.order.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/me/orders")
@PreAuthorize("isAuthenticated()")
public class CurrentUserOrderController {

    private final OrderService orderService;

    public CurrentUserOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<OrderResponseDTO> getCurrentUserOrders(@PageableDefault(size = 10) Pageable pageable) {
        return orderService.findMyOrders(pageable);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO checkout(@Valid @RequestBody OrderCheckoutRequestDTO request) {
        return orderService.placeOrderFromCart(request);
    }
}
