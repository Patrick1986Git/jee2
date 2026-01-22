package com.company.shop.module.order.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.order.dto.OrderCreateRequestDTO;
import com.company.shop.module.order.dto.OrderDetailedResponseDTO;
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
	private final PaymentRepository paymentRepo;
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
	@Transactional(readOnly = true)
	public OrderDetailedResponseDTO findById(UUID id) {
		Order order = orderRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Zamówienie nie istnieje"));

		User currentUser = userService.getCurrentUserEntity();

		boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
		boolean isOwner = order.getUser().getId().equals(currentUser.getId());

		if (!isAdmin && !isOwner) {
			throw new AccessDeniedException("Brak uprawnień do podglądu zamówienia");
		}

		return mapper.toDetailedDto(order);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<OrderResponseDTO> findAll(Pageable pageable) {
		// Ta metoda jest przeznaczona dla Admina (zabezpieczona w kontrolerze)
		return orderRepo.findAll(pageable).map(mapper::toDto);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<OrderResponseDTO> findMyOrders(Pageable pageable) {
		User currentUser = userService.getCurrentUserEntity();

		return orderRepo.findByUser(currentUser, pageable).map(mapper::toDto);
	}

	@Override
	@Transactional
	public OrderResponseDTO placeOrder(OrderCreateRequestDTO request) {
		User user = userService.getCurrentUserEntity();
		Order order = new Order(user, BigDecimal.ZERO);
		BigDecimal total = BigDecimal.ZERO;

		for (var itemRequest : request.getItems()) {
			Product product = productRepo.findById(itemRequest.getProductId()).orElseThrow(
					() -> new EntityNotFoundException("Produkt nie istnieje: " + itemRequest.getProductId()));

			if (product.getStock() < itemRequest.getQuantity()) {
				throw new IllegalStateException("Niewystarczająca ilość produktu: " + product.getName());
			}

			product.updateStock(product.getStock() - itemRequest.getQuantity());

			BigDecimal priceAtPurchase = product.getPrice();
			total = total.add(priceAtPurchase.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

			order.addItem(new OrderItem(product, itemRequest.getQuantity(), priceAtPurchase));
		}

		order.setTotalAmount(total);
		Order savedOrder = orderRepo.save(order);

		Payment payment = new Payment(savedOrder, "STRIPE", total);
		paymentRepo.save(payment);

		return mapper.toDto(savedOrder);
	}
}