package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.exception.ExchangeException;
import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.util.ResponseParser;
import com.erikandreas.exchangedataservice.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BinanceRestTemplateService implements BlockingPriceService {

    private final RestTemplate restTemplate;

    public BinanceRestTemplateService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Cacheable(value = "prices", key = "#symbol")
    public CryptoPrice getPrice(String symbol) {
        log.info("Fetching price from Binance for symbol: {} (cache miss)", symbol);
        ValidationUtils.validateSymbol(symbol);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity("https://api.binance.com/api/v3/ticker/price?symbol=" + symbol, String.class);

            if (!response
                    .getStatusCode()
                    .is2xxSuccessful()) {
                throw new ExchangeException(String.format("Binance API error: %s", response.getStatusCode()), null);
            }

            return ResponseParser.parseBinanceResponse(response.getBody(), symbol);

        } catch (RestClientException e) {
            throw new ExchangeException("Failed to fetch price from Binance", e);
        }
    }
}
