package com.diva.batch.fitme.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {

    public BusinessException() {
    }

    public BusinessException(final String message) {
        super(message);
    }

    public abstract HttpStatus status();
}
