package com.company.shop.module.order.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.company.shop.module.order.entity.Order;
import com.company.shop.module.user.entity.User;

public interface OrderRepository extends JpaRepository<Order, UUID> {
	Page<Order> findByUser(User user, Pageable pageable);
}