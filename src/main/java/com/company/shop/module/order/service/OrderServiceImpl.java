package com.company.shop.module.order.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.order.dto.OrderCreateRequestDTO;
import com.company.shop.module.order.dto.OrderResponseDTO;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.OrderItem;
import com.company.shop.module.order.entity.Payment;
import com.company.shop.module.order.mapper.OrderMapper;
import com.company.shop.module.order.repository.OrderRepository;
import com.company.shop.module.order.repository.PaymentRepository;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.repository.ProductRepository;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepo;
	private final ProductRepository productRepo;
	private final PaymentRepository paymentRepo; // Dodane!
	private final UserService userService;
	private final OrderMapper mapper;

	public OrderServiceImpl(OrderRepository orderRepo, ProductRepository productRepo, PaymentRepository paymentRepo,
			UserService userService, OrderMapper mapper) {
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.paymentRepo = paymentRepo;
		this.userService = userService;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public OrderResponseDTO placeOrder(OrderCreateRequestDTO request) {

		User user = userService.getCurrentUserEntity();

		// Tworzenie szkieletu zamówienia
		Order order = new Order(user, BigDecimal.ZERO);
		BigDecimal total = BigDecimal.ZERO;

		for (var itemRequest : request.getItems()) {
			Product product = productRepo.findById(itemRequest.getProductId()).orElseThrow(
					() -> new EntityNotFoundException("Produkt nie istnieje: " + itemRequest.getProductId()));

			if (product.getStock() < itemRequest.getQuantity()) {
				throw new IllegalStateException("Niewystarczająca ilość produktu: " + product.getName());
			}

			// Aktualizacja stanu magazynowego
			product.updateStock(product.getStock() - itemRequest.getQuantity());

			// Tworzenie pozycji zamówienia z ceną z MOMENTU zakupu
			BigDecimal priceAtPurchase = product.getPrice();
			BigDecimal itemTotal = priceAtPurchase.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
			total = total.add(itemTotal);

			order.addItem(new OrderItem(product, itemRequest.getQuantity(), priceAtPurchase));
		}

		// Ustawienie finalnej kwoty (Order powinien mieć setter lub metodę update)

		order.setTotalAmount(total);

		Order savedOrder = orderRepo.save(order);

		// Inicjacja płatności
		Payment payment = new Payment(savedOrder, "STRIPE", total);
		paymentRepo.save(payment);

		return mapper.toDto(savedOrder);
	}
}