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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Global interceptor for application-wide exception handling.
 *
 * <p>
 * This class is purely infrastructural. It does not contain domain knowledge
 * and does not map domain exceptions manually. Domain-specific errors must
 * extend {@link BusinessException}.
 * </p>
 *
 * @since 2.0.0
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

		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		log.warn("Input validation failed: {}", errors);

		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);

		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handles all domain/business exceptions.
	 *
	 * <p>
	 * Every domain exception must extend {@link BusinessException} and define its
	 * own HTTP status and message.
	 * </p>
	 *
	 * @param ex the business exception
	 * @return mapped HTTP response defined by the exception itself
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiError> handleBusinessException(BusinessException ex) {

		String errorCode = ex.getErrorCode() != null ? ex.getErrorCode() : "UNKNOWN_BUSINESS_ERROR";
		if (ex.getStatus().is5xxServerError()) {
			log.error("Business invariant/server exception occurred: {} [{}]", ex.getMessage(), errorCode, ex);
		} else {
			log.warn("Business exception occurred: {} [{}]", ex.getMessage(), errorCode);
		}

		ApiError apiError = new ApiError(ex.getStatus().value(), ex.getMessage(), errorCode);

		return new ResponseEntity<>(apiError, ex.getStatus());
	}


	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ApiError> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {

		log.warn("Optimistic locking conflict detected: {}", ex.getMessage());

		ApiError apiError = new ApiError(HttpStatus.CONFLICT.value(),
				"Resource was modified by another transaction. Please refresh and retry.",
				"OPTIMISTIC_LOCK_CONFLICT");

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

		ApiError apiError = new ApiError(HttpStatus.FORBIDDEN.value(),
				"Insufficient permissions to access this resource",
				"ACCESS_DENIED");

		return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
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
				"No endpoint found for: " + ex.getHttpMethod() + " " + ex.getRequestURL(),
				"ENDPOINT_NOT_FOUND");

		return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
	}

	/**
	 * Ultimate safety net for all unhandled exceptions.
	 *
	 * <p>
	 * Logs full stack trace internally while hiding implementation details from API
	 * consumers.
	 * </p>
	 *
	 * @param ex the caught exception
	 * @return a 500 Internal Server Error response
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {

		log.error("Unhandled application exception:", ex);

		ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected server error occurred. Please contact support if the problem persists.");

		return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}