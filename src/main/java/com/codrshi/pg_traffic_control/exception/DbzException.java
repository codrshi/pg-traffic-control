package com.codrshi.pg_traffic_control.exception;

import org.springframework.http.HttpStatus;

public class DbzException extends BaseException {

    public DbzException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
