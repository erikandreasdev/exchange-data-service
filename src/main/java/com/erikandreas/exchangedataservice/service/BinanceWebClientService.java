package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.util.ResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BinanceWebClientService implements ReactivePriceService {

    private final WebClient webClient;

    public BinanceWebClientService(WebClient binanceWebClient) {
        this.webClient = binanceWebClient;
    }

    @Override
    @Cacheable(value = "prices", key = "#symbol")
    public Mono<CryptoPrice> getPrice(String symbol) {
        log.info("Fetching price from Binance for symbol: {} (cache miss)", symbol);

        return webClient.get()
            .uri("/ticker/price?symbol={symbol}", symbol)
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> ResponseParser.parseBinanceResponse(response, symbol));
    }
}