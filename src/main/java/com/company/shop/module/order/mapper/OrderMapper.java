package com.company.shop.module.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.company.shop.module.order.dto.OrderResponseDTO;
import com.company.shop.module.order.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
	@Mapping(target = "totalAmount", source = "totalAmount")
	OrderResponseDTO toDto(Order order);
}