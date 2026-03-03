package com.company.shop.module.user.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when an operation requires an authenticated user principal.
 */
public class UserAuthenticationRequiredException extends BusinessException {

    public UserAuthenticationRequiredException() {
        super(HttpStatus.UNAUTHORIZED,
                "Authentication is required to access user profile data",
                UserErrorCodes.USER_AUTHENTICATION_REQUIRED);
    }
}
