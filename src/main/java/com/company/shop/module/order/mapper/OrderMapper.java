package com.company.shop.module.order.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.company.shop.module.order.dto.OrderDetailedResponseDTO;
import com.company.shop.module.order.dto.OrderItemResponseDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderMapper {

	OrderResponseDTO toDto(Order order);

	@Mapping(target = "userEmail", source = "user.email")
	OrderDetailedResponseDTO toDetailedDto(Order order);

	@Mapping(target = "productId", source = "product.id")
	@Mapping(target = "productName", source = "product.name")
	@Mapping(target = "sku", source = "product.sku")
	@Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
	OrderItemResponseDTO toItemDto(OrderItem item);

	default BigDecimal calculateSubtotal(OrderItem item) {
		return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
	}
}