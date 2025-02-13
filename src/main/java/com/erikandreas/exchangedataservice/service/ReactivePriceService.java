package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.model.CryptoPrice;
import reactor.core.publisher.Mono;

public interface ReactivePriceService {
    Mono<CryptoPrice> getPrice(String symbol);
}
