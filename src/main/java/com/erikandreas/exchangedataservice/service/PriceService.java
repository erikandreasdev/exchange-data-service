package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.model.CryptoPrice;
import reactor.core.publisher.Mono;

public interface PriceService {
    CryptoPrice getPriceBlocking(String symbol);    // For RestTemplate
    Mono<CryptoPrice> getPriceReactive(String symbol);  // For WebClient
}
