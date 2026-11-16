package com.codrshi.pg_traffic_control.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
