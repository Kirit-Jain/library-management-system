package com.library.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MembershipExpiredException extends RuntimeException {
    public MembershipExpiredException(String message) {
        super(message);
    }
}