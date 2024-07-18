package com.samatov.security.webflux.project.exception;

import lombok.Getter;

public class APIException extends RuntimeException {

    @Getter
    protected String errorCode;

    public APIException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
