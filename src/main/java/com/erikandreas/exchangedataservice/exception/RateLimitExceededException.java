package com.erikandreas.exchangedataservice.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String exchange) {
        super("Rate limit exceeded for exchange: " + exchange);
    }
}
