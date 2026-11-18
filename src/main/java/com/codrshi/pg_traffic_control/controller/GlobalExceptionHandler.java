package com.codrshi.pg_traffic_control.controller;

import com.codrshi.pg_traffic_control.dto.ErrorResponse;
import com.codrshi.pg_traffic_control.exception.ResourceNotFoundException;
import com.codrshi.pg_traffic_control.exception.UnprocessableContentException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = {MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request){
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(errorMessages.toString())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Validation error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).contentType(getContentType(request)).body(errorResponse);
    }

    @ExceptionHandler(exception = {OptimisticLockingFailureException.class, IllegalStateException.class})
    ResponseEntity<?> handleConflictException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponseDTO = ErrorResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Conflict occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(getContentType(request)).body(errorResponseDTO);
    }

    @ExceptionHandler(exception = {ResourceNotFoundException.class})
    ResponseEntity<?> handleNotFoundException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponseDTO = ErrorResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Resource not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(getContentType(request)).body(errorResponseDTO);
    }

    @ExceptionHandler(exception = {UnprocessableContentException.class})
    ResponseEntity<?> handleUnprocessableContentException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponseDTO = ErrorResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Invalid request: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).contentType(getContentType(request)).body(errorResponseDTO);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> handleUnknownException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponseDTO = ErrorResponse.builder()
                .message("An unexpected error occurred.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(getContentType(request)).body(errorResponseDTO);
    }

    private MediaType getContentType(HttpServletRequest request) {
        String acceptHeader = Optional.ofNullable(request.getHeader(HttpHeaders.ACCEPT))
                .orElse(MediaType.APPLICATION_JSON_VALUE);

        if (acceptHeader.contains("*/*") || acceptHeader.contains("*")) {
            return MediaType.APPLICATION_JSON;
        }

        return MediaType.parseMediaType(acceptHeader);
    }
}
