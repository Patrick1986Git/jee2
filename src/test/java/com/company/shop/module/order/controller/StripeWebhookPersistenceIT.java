package com.company.shop.module.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.company.shop.common.model.AuditableEntity;
import com.company.shop.module.cart.service.CartService;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.OrderStatus;
import com.company.shop.module.order.entity.Payment;
import com.company.shop.module.order.entity.PaymentStatus;
import com.company.shop.module.order.repository.OrderRepository;
import com.company.shop.module.order.repository.PaymentRepository;
import com.company.shop.module.user.entity.User;
import com.company.shop.module.user.repository.UserRepository;
import com.company.shop.persistence.support.PostgresContainerSupport;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class StripeWebhookPersistenceIT extends PostgresContainerSupport {

	private static final String WEBHOOK_URL = "/api/v1/webhooks/stripe";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@MockitoBean
	private CartService cartService;

	@Test
	void handleStripeWebhook_shouldPersistOrderAndPaymentAsCompletedWhenPaymentIntentSucceeded() throws Exception {
		SeededOrder seededOrder = seedOrderWithPayment(BigDecimal.valueOf(25), "pi_success");

		Event event = succeededEvent(seededOrder.order().getId().toString(), "pi_success", 2500L, "pln");

		try (var webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_placeholder"))
					.thenReturn(event);

			mockMvc.perform(post(WEBHOOK_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Stripe-Signature", "sig")
					.content("payload"))
					.andExpect(status().isOk());
		}

		Order updatedOrder = orderRepository.findById(seededOrder.order().getId()).orElseThrow();
		Payment updatedPayment = paymentRepository.findByOrderId(seededOrder.order().getId()).orElseThrow();

		assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
		verify(cartService).clearCartForUser(seededOrder.user().getId());
	}

	@Test
	void handleStripeWebhook_shouldKeepPersistenceStateUnchangedWhenWebhookTypeIsUnsupported() throws Exception {
		SeededOrder seededOrder = seedOrderWithPayment(BigDecimal.valueOf(30), "pi_pending");

		Event event = mock(Event.class);
		when(event.getType()).thenReturn("payment_intent.processing");

		try (var webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_placeholder"))
					.thenReturn(event);

			mockMvc.perform(post(WEBHOOK_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Stripe-Signature", "sig")
					.content("payload"))
					.andExpect(status().isOk());
		}

		Order unchangedOrder = orderRepository.findById(seededOrder.order().getId()).orElseThrow();
		Payment unchangedPayment = paymentRepository.findByOrderId(seededOrder.order().getId()).orElseThrow();

		assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
		assertThat(unchangedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		verifyNoInteractions(cartService);
	}

	@Test
	void handleStripeWebhook_shouldIgnoreDuplicateWhenOrderAlreadyPaid() throws Exception {
		SeededOrder seededOrder = seedOrderWithPayment(BigDecimal.valueOf(40), "pi_duplicate");
		markOrderAndPaymentAsCompleted(seededOrder.order().getId());

		Event event = succeededEvent(seededOrder.order().getId().toString(), "pi_duplicate", 4000L, "pln");

		try (var webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_placeholder"))
					.thenReturn(event);

			mockMvc.perform(post(WEBHOOK_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Stripe-Signature", "sig")
					.content("payload"))
					.andExpect(status().isOk());
		}

		Order unchangedOrder = orderRepository.findById(seededOrder.order().getId()).orElseThrow();
		Payment unchangedPayment = paymentRepository.findByOrderId(seededOrder.order().getId()).orElseThrow();

		assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(unchangedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
		verifyNoInteractions(cartService);
	}

	@Test
	void handleStripeWebhook_shouldKeepPersistenceStateUnchangedWhenPaymentAmountDoesNotMatchOrderTotal() throws Exception {
		SeededOrder seededOrder = seedOrderWithPayment(BigDecimal.valueOf(35), "pi_amount_mismatch");

		Event event = succeededEvent(seededOrder.order().getId().toString(), "pi_amount_mismatch", 3499L, "pln");

		try (var webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_placeholder"))
					.thenReturn(event);

			mockMvc.perform(post(WEBHOOK_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Stripe-Signature", "sig")
					.content("payload"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errorCode").value("STRIPE_WEBHOOK_SIGNATURE_INVALID"));
		}

		Order unchangedOrder = orderRepository.findById(seededOrder.order().getId()).orElseThrow();
		Payment unchangedPayment = paymentRepository.findByOrderId(seededOrder.order().getId()).orElseThrow();

		assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
		assertThat(unchangedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		verifyNoInteractions(cartService);
	}

	@Test
	void handleStripeWebhook_shouldKeepPersistenceStateUnchangedWhenPaymentCurrencyDoesNotMatchOrderCurrency() throws Exception {
		SeededOrder seededOrder = seedOrderWithPayment(BigDecimal.valueOf(55), "pi_currency_mismatch");

		Event event = succeededEvent(seededOrder.order().getId().toString(), "pi_currency_mismatch", 5500L, "eur");

		try (var webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_placeholder"))
					.thenReturn(event);

			mockMvc.perform(post(WEBHOOK_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.header("Stripe-Signature", "sig")
					.content("payload"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.errorCode").value("STRIPE_WEBHOOK_SIGNATURE_INVALID"));
		}

		Order unchangedOrder = orderRepository.findById(seededOrder.order().getId()).orElseThrow();
		Payment unchangedPayment = paymentRepository.findByOrderId(seededOrder.order().getId()).orElseThrow();

		assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.NEW);
		assertThat(unchangedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		verifyNoInteractions(cartService);
	}

	private SeededOrder seedOrderWithPayment(BigDecimal orderAmount, String paymentIntentId) {
		User user = userRepository.save(new User("stripe-webhook-" + paymentIntentId + "@example.com", "encoded-pass", "Test", "User"));

		Order order = new Order(user);
		setOrderTotal(order, orderAmount);
		Order savedOrder = orderRepository.save(order);

		Payment payment = new Payment(savedOrder, "STRIPE", orderAmount);
		payment.attachProviderPayment(paymentIntentId, "cs_" + paymentIntentId);
		paymentRepository.save(payment);

		return new SeededOrder(user, savedOrder);
	}

	private Event succeededEvent(String orderId, String paymentIntentId, long amountReceived, String currency) {
		Event event = mock(Event.class);
		EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
		PaymentIntent paymentIntent = mock(PaymentIntent.class);

		when(event.getType()).thenReturn("payment_intent.succeeded");
		when(event.getDataObjectDeserializer()).thenReturn(deserializer);
		when(deserializer.getObject()).thenReturn(java.util.Optional.of(paymentIntent));
		when(paymentIntent.getMetadata()).thenReturn(Map.of("orderId", orderId));
		when(paymentIntent.getAmountReceived()).thenReturn(amountReceived);
		when(paymentIntent.getCurrency()).thenReturn(currency);
		when(paymentIntent.getId()).thenReturn(paymentIntentId);

		return event;
	}

	private void markOrderAndPaymentAsCompleted(java.util.UUID orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow();
		order.markAsPaid();
		orderRepository.save(order);

		Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
		payment.markAsCompleted();
		paymentRepository.save(payment);
	}

	private void setOrderTotal(Order order, BigDecimal amount) {
		try {
			Field totalAmountField = Order.class.getDeclaredField("totalAmount");
			totalAmountField.setAccessible(true);
			totalAmountField.set(order, amount);

			Field createdAtField = AuditableEntity.class.getDeclaredField("createdAt");
			createdAtField.setAccessible(true);
			createdAtField.set(order, LocalDateTime.now());
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to set order amount for integration test", e);
		}
	}

	private record SeededOrder(User user, Order order) {
	}
}
