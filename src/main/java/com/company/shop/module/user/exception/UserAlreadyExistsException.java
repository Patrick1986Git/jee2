package com.company.shop.module.user.exception;

import org.springframework.http.HttpStatus;

import com.company.shop.common.exception.BusinessException;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException() {
        super(HttpStatus.CONFLICT,
                "User account already exists",
                UserErrorCodes.USER_ALREADY_EXISTS);
    }
}
