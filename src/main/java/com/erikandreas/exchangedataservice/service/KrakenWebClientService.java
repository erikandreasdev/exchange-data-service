package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.exception.ExchangeException;
import com.erikandreas.exchangedataservice.exception.RateLimitExceededException;
import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.model.dto.KrakenTickerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class KrakenWebClientService implements ReactivePriceService {

    private final WebClient webClient;
    private final RateLimitService rateLimitService;

    public KrakenWebClientService(
            @Qualifier("krakenWebClient") WebClient webClient,
            RateLimitService rateLimitService) {
        this.webClient = webClient;
        this.rateLimitService = rateLimitService;
    }

    @Override
    @Cacheable(value = "prices", key = "'kraken-reactive:' + #symbol")
    public Mono<CryptoPrice> getPrice(String symbol) {
        return Mono.fromSupplier(() -> rateLimitService.tryConsume("kraken"))
            .flatMap(canProceed -> {
                if (!canProceed) {
                    return Mono.error(new RateLimitExceededException("kraken"));
                }

                log.info("Fetching price from Kraken for symbol: {} (cache miss)", symbol);
                String krakenSymbol = convertToKrakenSymbol(symbol);

                return webClient.get()
                    .uri("/Ticker?pair={symbol}", krakenSymbol)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(
                            new ExchangeException(
                                "Kraken API error: " + response.statusCode(),
                                null
                            )
                        )
                    )
                    .bodyToMono(KrakenTickerDTO.class)
                    .map(dto -> {
                        if (dto.getError() != null && !dto.getError().isEmpty()) {
                            throw new ExchangeException("Kraken API error: " + dto.getError().get(0), null);
                        }
                        return mapToCryptoPrice(dto, symbol, krakenSymbol);
                    })
                    .doOnError(error -> log.error("Error fetching from Kraken: {}", error.getMessage()));
            });
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