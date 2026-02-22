/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
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
     * Handles conflicts during entity creation (e.g., unique constraint violations).
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
     * Handles cases where a requested product does not exist.
     *
     * @param ex the domain-specific product not found exception.
     * @return a 404 Not Found response.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFound(ProductNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles cases where a cart is not found for the requested user.
     *
     * @param ex the domain-specific cart not found exception.
     * @return a 404 Not Found response.
     */
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiError> handleCartNotFound(CartNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles cases where requested quantity exceeds available stock.
     *
     * @param ex the domain-specific insufficient stock exception.
     * @return a 409 Conflict response.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
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
     * Handles invalid argument exceptions (e.g., duplicate SKU, category name already exists).
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
     * Handles illegal state exceptions (e.g., empty cart on checkout, insufficient stock).
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