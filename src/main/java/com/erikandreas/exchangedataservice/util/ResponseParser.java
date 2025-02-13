package com.erikandreas.exchangedataservice.util;

import com.erikandreas.exchangedataservice.exception.ExchangeException;
import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
@Slf4j
public class ResponseParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CryptoPrice parseBinanceResponse(String response, String symbol) {
        try {
            JsonNode root = objectMapper.readTree(response);
            validateBinanceResponse(root);

            return CryptoPrice.builder()
                .symbol(symbol)
                .price(new BigDecimal(root.get("price").asText()))
                .exchange("Binance")
                .timestamp(LocalDateTime.now())
                .build();

        } catch (JsonProcessingException e) {
            throw new ExchangeException("Failed to parse Binance response", e);
        } catch (NumberFormatException e) {
            throw new ExchangeException("Invalid price format in response", e);
        }
    }

    private static void validateBinanceResponse(JsonNode root) {
        if (!root.has("price")) {
            throw new ExchangeException("Invalid response format from Binance", null);
        }
    }
}