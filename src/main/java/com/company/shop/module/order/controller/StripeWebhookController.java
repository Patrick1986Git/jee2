package com.company.shop.module.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.order.service.PaymentService;

@RestController
@RequestMapping("/api/webhooks/stripe")
public class StripeWebhookController {

	private final PaymentService paymentService;

	public StripeWebhookController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload,
			@RequestHeader("Stripe-Signature") String sigHeader) {

		paymentService.handleWebhook(payload, sigHeader);
		return ResponseEntity.ok().build();
	}
}