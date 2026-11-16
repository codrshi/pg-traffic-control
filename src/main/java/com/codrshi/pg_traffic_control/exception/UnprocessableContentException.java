package com.codrshi.pg_traffic_control.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableContentException extends BaseException {

    public UnprocessableContentException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
