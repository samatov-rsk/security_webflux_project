package com.samatov.security.webflux.project.exception;

public class APIException extends RuntimeException {

    protected String errorCode;

    public APIException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
