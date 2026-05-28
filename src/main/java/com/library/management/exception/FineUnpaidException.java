package com.library.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class FineUnpaidException extends RuntimeException {
    public FineUnpaidException(String message) {
        super(message);
    }
}