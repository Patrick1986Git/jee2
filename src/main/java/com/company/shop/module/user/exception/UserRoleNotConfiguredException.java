package com.company.shop.module.user.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

/**
 * Exception thrown when a required system role is missing in persistent storage.
 */
public class UserRoleNotConfiguredException extends BusinessException {

    public UserRoleNotConfiguredException(String roleName) {
        super(HttpStatus.INTERNAL_SERVER_ERROR,
                "Required system role is not configured: " + roleName,
                UserErrorCodes.USER_ROLE_NOT_CONFIGURED);
    }
}
