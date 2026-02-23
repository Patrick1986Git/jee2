package com.company.shop.module.user.exception;

import org.springframework.http.HttpStatus;
import com.company.shop.common.exception.BusinessException;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "User with email " + email + " already exists", "USER_ALREADY_EXISTS");
    }
}