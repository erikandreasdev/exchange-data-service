package com.erikandreas.exchangedataservice.exception;

public class InvalidSymbolException extends RuntimeException {
    public InvalidSymbolException(String symbol) {
        super("Invalid symbol: " + symbol);
    }
}
