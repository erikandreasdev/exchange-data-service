package com.erikandreas.exchangedataservice.exception;

import com.erikandreas.exchangedataservice.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidSymbolException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSymbol(InvalidSymbolException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "INVALID_SYMBOL",
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExchangeException.class)
    public ResponseEntity<ErrorResponse> handleExchangeError(ExchangeException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            "EXCHANGE_ERROR",
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "An unexpected error occurred",
                "INTERNAL_ERROR",
                LocalDateTime.now()
        );
        log.error("Unexpected error", ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
