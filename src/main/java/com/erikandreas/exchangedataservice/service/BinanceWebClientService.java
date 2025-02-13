package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.exception.ExchangeException;
import com.erikandreas.exchangedataservice.exception.RateLimitExceededException;
import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.util.ResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BinanceWebClientService implements ReactivePriceService {

    private final WebClient webClient;
    private final RateLimitService rateLimitService;

    public BinanceWebClientService(
            @Qualifier("binanceWebClient") WebClient webClient,
            RateLimitService rateLimitService) {
        this.webClient = webClient;
        this.rateLimitService = rateLimitService;
    }

    @Override
    @Cacheable(value = "prices", key = "'binance-reactive:' + #symbol")
    public Mono<CryptoPrice> getPrice(String symbol) {
        return Mono.fromSupplier(() -> rateLimitService.tryConsume("binance"))
            .flatMap(canProceed -> {
                if (!canProceed) {
                    return Mono.error(new RateLimitExceededException("binance"));
                }

                log.info("Fetching price from Binance for symbol: {} (cache miss)", symbol);

                return webClient.get()
                    .uri("/ticker/price?symbol={symbol}", symbol)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(
                            new ExchangeException(
                                "Binance API error: " + response.statusCode(),
                                null
                            )
                        )
                    )
                    .bodyToMono(String.class)
                    .map(response -> ResponseParser.parseBinanceResponse(response, symbol))
                    .doOnError(error -> log.error("Error fetching from Binance: {}", error.getMessage()));
            });
    }
}