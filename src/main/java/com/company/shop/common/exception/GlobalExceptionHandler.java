/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 *
 * This software is the confidential and proprietary information of Your Company Name.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Your Company.
 */

package com.company.shop.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;

/**
 * Global interceptor for application-wide exception handling.
 * <p>
 * This class captures exceptions thrown by any controller and transforms them 
 * into standardized {@link ApiError} responses. It also ensures that critical 
 * system failures are properly logged for diagnostic purposes.
 * </p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation failures for {@code @Valid} annotated parameters.
     *
     * @param ex the exception containing binding and validation results.
     * @return a 400 Bad Request response with detailed field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Input validation failed: {}", errors);

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles conflicts during user registration (e.g., email already in use).
     *
     * @param ex the domain-specific exception for existing users.
     * @return a 409 Conflict response.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles conflicts when a resource with the same unique identifier already exists
     * (e.g., duplicate product SKU or category name).
     *
     * @param ex the domain-specific exception for duplicate resources.
     * @return a 409 Conflict response.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResource(DuplicateResourceException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles order placement attempts when the shopping cart is empty.
     *
     * @param ex the domain-specific exception for an empty cart.
     * @return a 409 Conflict response.
     */
    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiError> handleEmptyCart(EmptyCartException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles cart and order operations rejected due to insufficient product stock.
     *
     * @param ex the domain-specific exception for stock shortages.
     * @return a 409 Conflict response.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles checkout failures caused by an invalid or expired discount code.
     *
     * @param ex the domain-specific exception for bad discount codes.
     * @return a 422 Unprocessable Entity response.
     */
    @ExceptionHandler(InvalidDiscountCodeException.class)
    public ResponseEntity<ApiError> handleInvalidDiscountCode(InvalidDiscountCodeException ex) {
        ApiError apiError = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handles attempts to submit a second review for the same product.
     *
     * @param ex the domain-specific exception for duplicate product reviews.
     * @return a 409 Conflict response.
     */
    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles failures originating from the external payment gateway.
     *
     * @param ex the domain-specific exception wrapping payment provider errors.
     * @return a 502 Bad Gateway response.
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiError> handlePaymentProcessing(PaymentProcessingException ex) {
        log.error("Payment gateway error: ", ex);
        ApiError apiError = new ApiError(HttpStatus.BAD_GATEWAY.value(),
                "Payment processing failed. Please try again or contact support.");
        return new ResponseEntity<>(apiError, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handles cases where a requested resource does not exist in the database.
     *
     * @param ex the JPA entity not found exception.
     * @return a 404 Not Found response.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles authorization failures.
     *
     * @param ex the access denied exception.
     * @return a 403 Forbidden response.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN.value(), "Insufficient permissions to access this resource");
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles invalid argument exceptions as a fallback for unclassified bad input.
     *
     * @param ex the exception describing the invalid input.
     * @return a 400 Bad Request response.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles illegal state exceptions as a fallback for unclassified state conflicts.
     *
     * @param ex the exception describing the conflicting state.
     * @return a 409 Conflict response.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles requests targeting undefined endpoints.
     *
     * @param ex the exception for a missing request mapping.
     * @return a 404 Not Found response.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandlerFound(NoHandlerFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(),
                "No endpoint found for: " + ex.getHttpMethod() + " " + ex.getRequestURL());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Ultimate safety net for all unhandled runtime exceptions.
     * <p>
     * Logs the full stack trace at ERROR level to facilitate troubleshooting 
     * while hiding sensitive internal details from the end user.
     * </p>
     *
     * @param ex the caught exception.
     * @return a 500 Internal Server Error response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {
        log.error("Unhandled application exception: ", ex);

        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected server error occurred. Please contact support if the problem persists.");
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}