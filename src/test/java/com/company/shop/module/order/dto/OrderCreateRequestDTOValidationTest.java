package com.company.shop.module.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;

class OrderCreateRequestDTOValidationTest {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validate_shouldReturnMinConstraintViolationWhenNestedOrderItemQuantityIsZero() {
        OrderCreateRequestDTO request = new OrderCreateRequestDTO();
        setItems(request, List.of(orderItem(UUID.randomUUID(), 0)));

        Set<ConstraintViolation<OrderCreateRequestDTO>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("items[0].quantity");
                    assertThat(violation.getConstraintDescriptor().getAnnotation().annotationType()).isEqualTo(Min.class);
                });
    }

    private static OrderCreateRequestDTO.OrderItemRequestDTO orderItem(UUID productId, int quantity) {
        OrderCreateRequestDTO.OrderItemRequestDTO item = new OrderCreateRequestDTO.OrderItemRequestDTO();
        setProductId(item, productId);
        setQuantity(item, quantity);
        return item;
    }

    private static void setItems(OrderCreateRequestDTO request, List<OrderCreateRequestDTO.OrderItemRequestDTO> items) {
        try {
            var field = OrderCreateRequestDTO.class.getDeclaredField("items");
            field.setAccessible(true);
            field.set(request, items);
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot set items field for test setup", ex);
        }
    }

    private static void setProductId(OrderCreateRequestDTO.OrderItemRequestDTO item, UUID productId) {
        try {
            var field = OrderCreateRequestDTO.OrderItemRequestDTO.class.getDeclaredField("productId");
            field.setAccessible(true);
            field.set(item, productId);
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot set productId field for test setup", ex);
        }
    }

    private static void setQuantity(OrderCreateRequestDTO.OrderItemRequestDTO item, int quantity) {
        try {
            var field = OrderCreateRequestDTO.OrderItemRequestDTO.class.getDeclaredField("quantity");
            field.setAccessible(true);
            field.set(item, quantity);
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot set quantity field for test setup", ex);
        }
    }
}
