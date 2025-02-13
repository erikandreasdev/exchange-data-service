package com.erikandreas.exchangedataservice.exception;

import com.erikandreas.exchangedataservice.model.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        ApiErrorResponse error = ApiErrorResponse
                .builder()
                .message(ex.getMessage())
                .errorCode("RATE_LIMIT_EXCEEDED")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(ExchangeException.class)
    public ResponseEntity<ApiErrorResponse> handleExchange(ExchangeException ex) {
        ApiErrorResponse error = ApiErrorResponse
                .builder()
                .message(ex.getMessage())
                .errorCode("EXCHANGE_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ApiErrorResponse error = ApiErrorResponse
                .builder()
                .message(ex.getMessage())
                .errorCode("INVALID_INPUT")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        ApiErrorResponse error = ApiErrorResponse
                .builder()
                .message("An unexpected error occurred. Please try again later.")
                .errorCode("INTERNAL_ERROR")
                .timestamp(LocalDateTime.now())
                .details(ex.getMessage())  // Only in development
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}