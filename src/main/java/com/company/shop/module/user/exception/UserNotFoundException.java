package com.company.shop.module.user.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String identifier) {
        super(HttpStatus.NOT_FOUND,
                "User not found: " + identifier,
                "USER_NOT_FOUND");
    }
}
