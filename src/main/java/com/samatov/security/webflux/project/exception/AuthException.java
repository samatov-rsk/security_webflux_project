package com.samatov.security.webflux.project.exception;

public class AuthException extends APIException {
    public AuthException(String message, String errorCode) {
        super(message, errorCode);
    }
}
