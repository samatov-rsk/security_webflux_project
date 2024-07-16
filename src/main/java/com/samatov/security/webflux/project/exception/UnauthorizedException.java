package com.samatov.security.webflux.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends APIException {
    public UnauthorizedException(String message) {
        super(message, "PROSELYTE_UNAUTHORIZED");
    }
}
