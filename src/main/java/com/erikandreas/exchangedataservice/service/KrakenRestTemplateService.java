package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.exception.ExchangeException;
import com.erikandreas.exchangedataservice.exception.RateLimitExceededException;
import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.model.dto.KrakenTickerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class KrakenRestTemplateService implements BlockingPriceService {

    private final RestTemplate restTemplate;
    private final RateLimitService rateLimitService;

    public KrakenRestTemplateService(RateLimitService rateLimitService) {
        this.restTemplate = new RestTemplate();
        this.rateLimitService = rateLimitService;
    }

    @Override
    @Cacheable(value = "prices", key = "'kraken-blocking:' + #symbol")
    public CryptoPrice getPrice(String symbol) {
        log.info("Fetching price from Kraken for symbol: {} (cache miss)", symbol);

        if (!rateLimitService.tryConsume("kraken")) {
            throw new RateLimitExceededException("kraken");
        }

        String krakenSymbol = convertToKrakenSymbol(symbol);

        try {
            ResponseEntity<KrakenTickerDTO> response = restTemplate.getForEntity(
                "https://api.kraken.com/0/public/Ticker?pair=" + krakenSymbol,
                KrakenTickerDTO.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ExchangeException(
                    String.format("Kraken API error: %s", response.getStatusCode()),
                    null
                );
            }

            KrakenTickerDTO dto = response.getBody();
            if (dto.getError() != null && !dto.getError().isEmpty()) {
                throw new ExchangeException("Kraken API error: " + dto.getError().get(0), null);
            }

            return mapToCryptoPrice(dto, symbol, krakenSymbol);

        } catch (RestClientException e) {
            throw new ExchangeException("Failed to fetch price from Kraken", e);
        }
    }

    private String convertToKrakenSymbol(String symbol) {
        if (symbol.startsWith("BTC")) {
            return "XBT" + symbol.substring(3);
        }
        return symbol;
    }

    private CryptoPrice mapToCryptoPrice(KrakenTickerDTO dto, String originalSymbol, String krakenSymbol) {
        KrakenTickerDTO.KrakenTickerInfo info = dto.getResult().get(krakenSymbol);
        if (info == null || info.getAsk() == null || info.getAsk().isEmpty()) {
            throw new ExchangeException("Invalid response format from Kraken", null);
        }

        return CryptoPrice.builder()
            .symbol(originalSymbol)
            .price(new BigDecimal(info.getAsk().get(0)))
            .exchange("Kraken")
            .timestamp(LocalDateTime.now())
            .build();
    }
}