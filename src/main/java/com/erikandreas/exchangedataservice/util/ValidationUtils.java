package com.erikandreas.exchangedataservice.util;

import com.erikandreas.exchangedataservice.exception.InvalidSymbolException;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtils {
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Z]{2,8}$");

    public static String validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidSymbolException("Symbol cannot be empty");
        }
        if (!SYMBOL_PATTERN.matcher(symbol).matches()) {
            throw new InvalidSymbolException(
                String.format("Symbol '%s' must be 2-8 uppercase letters", symbol)
            );
        }
        return symbol;
    }
}
