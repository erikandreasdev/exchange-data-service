package com.erikandreas.exchangedataservice.controller;

import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prices")
@Slf4j
public class PriceController {

    private final Map<String, BlockingPriceService> blockingServices;
    private final Map<String, ReactivePriceService> reactiveServices;

    public PriceController(
            BinanceRestTemplateService binanceBlockingService,
            KrakenRestTemplateService krakenBlockingService,
            BinanceWebClientService binanceReactiveService,
            KrakenWebClientService krakenReactiveService) {

        this.blockingServices = Map.of(
            "binance", binanceBlockingService,
            "kraken", krakenBlockingService
        );

        this.reactiveServices = Map.of(
            "binance", binanceReactiveService,
            "kraken", krakenReactiveService
        );
    }

    // Regular blocking endpoint
    @GetMapping("/blocking/{exchange}/{symbol}")
    public CryptoPrice getPrice(
            @PathVariable String exchange,
            @PathVariable String symbol) {
        BlockingPriceService service = blockingServices.get(exchange.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported exchange: " + exchange);
        }
        return service.getPrice(symbol);
    }

    // Reactive endpoint
    @GetMapping("/reactive/{exchange}/{symbol}")
    public Mono<CryptoPrice> getPriceReactive(
            @PathVariable String exchange,
            @PathVariable String symbol) {
        ReactivePriceService service = reactiveServices.get(exchange.toLowerCase());
        if (service == null) {
            return Mono.error(new IllegalArgumentException("Unsupported exchange: " + exchange));
        }
        return service.getPrice(symbol);
    }

    // Helper method to list available exchanges
    @GetMapping("/exchanges")
    public Map<String, List<String>> getAvailableExchanges() {
        return Map.of(
            "blocking", new ArrayList<>(blockingServices.keySet()),
            "reactive", new ArrayList<>(reactiveServices.keySet())
        );
    }
}