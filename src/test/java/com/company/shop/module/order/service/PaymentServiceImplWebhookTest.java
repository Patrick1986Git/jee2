package com.company.shop.module.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.shop.common.model.BaseEntity;
import com.company.shop.module.cart.service.CartService;
import com.company.shop.module.category.entity.Category;
import com.company.shop.module.order.entity.Order;
import com.company.shop.module.order.entity.OrderItem;
import com.company.shop.module.order.entity.OrderStatus;
import com.company.shop.module.order.entity.Payment;
import com.company.shop.module.order.exception.OrderNotFoundException;
import com.company.shop.module.order.exception.WebhookProcessingException;
import com.company.shop.module.order.exception.WebhookSignatureInvalidException;
import com.company.shop.module.order.repository.OrderRepository;
import com.company.shop.module.order.repository.PaymentRepository;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.user.entity.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplWebhookTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private CartService cartService;

	private PaymentServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new PaymentServiceImpl(orderRepository, paymentRepository, cartService);
		setField(service, "webhookSecret", "whsec_test_123");
	}

	@Test
	void handleWebhook_shouldIgnoreWhenEventTypeIsNotPaymentIntentSucceeded() {
		Event event = mock(Event.class);
		when(event.getType()).thenReturn("payment_intent.processing");

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			service.handleWebhook("payload", "sig");

			verifyNoInteractions(orderRepository, paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldIgnoreWhenDeserializedPaymentIntentIsMissing() {
		Event event = mock(Event.class);
		EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

		when(event.getType()).thenReturn("payment_intent.succeeded");
		when(event.getDataObjectDeserializer()).thenReturn(deserializer);
		when(deserializer.getObject()).thenReturn(Optional.empty());

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			service.handleWebhook("payload", "sig");

			verifyNoInteractions(orderRepository, paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenOrderIdMetadataMissing() {
		Event event = mock(Event.class);
		EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
		PaymentIntent intent = mock(PaymentIntent.class);

		when(event.getType()).thenReturn("payment_intent.succeeded");
		when(event.getDataObjectDeserializer()).thenReturn(deserializer);
		when(deserializer.getObject()).thenReturn(Optional.of(intent));
		when(intent.getMetadata()).thenReturn(Map.of());

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookSignatureInvalidException.class).hasMessageContaining("orderId");

			verifyNoInteractions(orderRepository, paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenOrderIdMetadataIsNotValidUuid() {
		Event event = mock(Event.class);
		EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
		PaymentIntent intent = mock(PaymentIntent.class);

		when(event.getType()).thenReturn("payment_intent.succeeded");
		when(event.getDataObjectDeserializer()).thenReturn(deserializer);
		when(deserializer.getObject()).thenReturn(Optional.of(intent));
		when(intent.getMetadata()).thenReturn(Map.of("orderId", "not-a-uuid"));

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookSignatureInvalidException.class);

			verifyNoInteractions(orderRepository, paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenPayloadOrSignatureInvalid() {
		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123"))
					.thenThrow(mock(SignatureVerificationException.class));

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookSignatureInvalidException.class);

			verifyNoInteractions(orderRepository, paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldIgnoreDuplicateWhenOrderAlreadyPaid() {
		Order paidOrder = orderWithTotal(BigDecimal.valueOf(25));
		paidOrder.markAsPaid();
		PaymentIntent intent = paymentIntentWithMetadata(paidOrder.getId());
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(paidOrder.getId())).thenReturn(Optional.of(paidOrder));

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			service.handleWebhook("payload", "sig");

			verify(orderRepository).findByIdForUpdate(paidOrder.getId());
			verify(orderRepository, never()).save(paidOrder);
			verifyNoInteractions(paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenOrderNotFoundForWebhookOrderId() {
		UUID orderId = UUID.randomUUID();
		PaymentIntent intent = paymentIntentWithMetadata(orderId);
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.empty());

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig")).isInstanceOf(OrderNotFoundException.class)
					.hasMessageContaining(orderId.toString());

			verify(orderRepository).findByIdForUpdate(orderId);
			verifyNoInteractions(paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenPaymentRecordMissing() {
		Order order = orderWithTotal(BigDecimal.valueOf(19.99));
		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceivedAndCurrency(order.getId(), 1999L, "pln");
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
		when(orderRepository.save(order)).thenReturn(order);
		when(paymentRepository.findByOrderIdForUpdate(order.getId())).thenReturn(Optional.empty());

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookProcessingException.class)
					.hasMessageContaining("Unable to process Stripe webhook event.");

			verify(orderRepository).save(order);
			verify(paymentRepository).findByOrderIdForUpdate(order.getId());
			verify(paymentRepository, never()).save(any(Payment.class));
			verify(cartService, never()).clearCartForUser(order.getUser().getId());
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenStoredProviderPaymentIdDiffersFromWebhookIntentId() {
		Order order = orderWithTotal(BigDecimal.valueOf(25));
		Payment payment = new Payment(order, "STRIPE", order.getTotalAmount());
		payment.attachProviderPayment("pi_other", "cs_any");

		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceivedCurrencyAndId(order.getId(), 2500L, "pln",
				"pi_actual");
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
		when(orderRepository.save(order)).thenReturn(order);
		when(paymentRepository.findByOrderIdForUpdate(order.getId())).thenReturn(Optional.of(payment));

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookSignatureInvalidException.class).hasMessageContaining("does not match");

			verify(orderRepository).save(order);
			verify(paymentRepository).findByOrderIdForUpdate(order.getId());
			assertThat(payment.getStatus()).isEqualTo(com.company.shop.module.order.entity.PaymentStatus.PENDING);
			verify(paymentRepository, never()).save(payment);
			verify(cartService, never()).clearCartForUser(order.getUser().getId());
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenAmountDoesNotMatchOrderTotal() {
		Order order = orderWithTotal(BigDecimal.valueOf(19.99));
		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceived(order.getId(), 1500L);
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookSignatureInvalidException.class).hasMessageContaining("amount does not match");

			verify(orderRepository, never()).save(order);
			verifyNoInteractions(paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldThrowWhenCurrencyIsNotPln() {
		Order order = orderWithTotal(BigDecimal.valueOf(19.99));
		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceivedAndCurrency(order.getId(), 1999L, "eur");
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookSignatureInvalidException.class)
					.hasMessageContaining("currency does not match");

			verify(orderRepository, never()).save(order);
			verifyNoInteractions(paymentRepository, cartService);
		}
	}

	@Test
	void handleWebhook_shouldWrapUnexpectedExceptionIntoWebhookProcessingException() {
		Order order = orderWithTotal(BigDecimal.valueOf(19.99));
		Payment payment = new Payment(order, "STRIPE", order.getTotalAmount());
		payment.attachProviderPayment("pi_123", "cs_123");

		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceivedCurrencyAndId(order.getId(), 1999L, "pln",
				"pi_123");
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
		when(orderRepository.save(order)).thenReturn(order);
		when(paymentRepository.findByOrderIdForUpdate(order.getId())).thenReturn(Optional.of(payment));
		when(paymentRepository.save(payment)).thenThrow(new RuntimeException("db write failed"));

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			assertThatThrownBy(() -> service.handleWebhook("payload", "sig"))
					.isInstanceOf(WebhookProcessingException.class)
					.hasMessageContaining("Unable to process Stripe webhook event.");

			verify(orderRepository).save(order);
			verify(paymentRepository).save(payment);
			verify(cartService, never()).clearCartForUser(order.getUser().getId());
		}
	}

	@Test
	void handleWebhook_shouldMarkOrderPaidPaymentCompletedAndClearCartWhenSucceededEventValid() {
		Order order = orderWithTotal(BigDecimal.valueOf(19.99));
		Payment payment = new Payment(order, "STRIPE", order.getTotalAmount());
		payment.attachProviderPayment("pi_123", "cs_123");

		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceivedCurrencyAndId(order.getId(), 1999L, "PLN",
				"pi_123");
		Event event = succeededEvent(intent);

		when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
		when(orderRepository.save(order)).thenReturn(order);
		when(paymentRepository.findByOrderIdForUpdate(order.getId())).thenReturn(Optional.of(payment));
		when(paymentRepository.save(payment)).thenReturn(payment);

		try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class)) {
			webhookStatic.when(() -> Webhook.constructEvent("payload", "sig", "whsec_test_123")).thenReturn(event);

			service.handleWebhook("payload", "sig");

			assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
			assertThat(payment.getStatus()).isEqualTo(com.company.shop.module.order.entity.PaymentStatus.COMPLETED);
			verify(orderRepository).save(order);
			verify(paymentRepository).save(payment);
			verify(cartService).clearCartForUser(order.getUser().getId());
		}
	}

	private Event succeededEvent(PaymentIntent intent) {
		Event event = mock(Event.class);
		EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
		when(event.getType()).thenReturn("payment_intent.succeeded");
		when(event.getDataObjectDeserializer()).thenReturn(deserializer);
		when(deserializer.getObject()).thenReturn(Optional.of(intent));
		return event;
	}

	private PaymentIntent paymentIntentWithMetadata(UUID orderId) {
		PaymentIntent intent = mock(PaymentIntent.class);
		when(intent.getMetadata()).thenReturn(Map.of("orderId", orderId.toString()));
		return intent;
	}

	private PaymentIntent paymentIntentWithMetadataAndAmountReceived(UUID orderId, Long amountReceived) {
		PaymentIntent intent = paymentIntentWithMetadata(orderId);
		when(intent.getAmountReceived()).thenReturn(amountReceived);
		return intent;
	}

	private PaymentIntent paymentIntentWithMetadataAndAmountReceivedAndCurrency(UUID orderId, Long amountReceived,
			String currency) {
		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceived(orderId, amountReceived);
		when(intent.getCurrency()).thenReturn(currency);
		return intent;
	}

	private PaymentIntent paymentIntentWithMetadataAndAmountReceivedCurrencyAndId(UUID orderId, Long amountReceived,
			String currency, String intentId) {
		PaymentIntent intent = paymentIntentWithMetadataAndAmountReceivedAndCurrency(orderId, amountReceived, currency);
		when(intent.getId()).thenReturn(intentId);
		return intent;
	}

	private Order orderWithTotal(BigDecimal unitPrice) {
		User user = new User("john@example.com", "encoded", "John", "Doe");
		setEntityId(user, UUID.randomUUID());

		Category category = new Category("Accessories", "accessories", "desc");
		Product product = new Product("Cable", "cable", "SKU-1", "desc", unitPrice, 10, category);
		setEntityId(product, UUID.randomUUID());

		Order order = new Order(user);
		setEntityId(order, UUID.randomUUID());
		order.addItem(new OrderItem(product, 1, unitPrice));
		return order;
	}

	private void setEntityId(Object entity, UUID id) {
		try {
			Field field = BaseEntity.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(entity, id);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		}
	}
}