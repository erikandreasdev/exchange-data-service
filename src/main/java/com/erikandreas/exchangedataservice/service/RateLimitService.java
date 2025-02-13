package com.erikandreas.exchangedataservice.service;

import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class RateLimitService {
    private final Map<String, Bucket> buckets;

    public RateLimitService(
            @Qualifier("binanceBucket") Bucket binanceBucket,
            @Qualifier("krakenBucket") Bucket krakenBucket) {
        this.buckets = Map.of(
            "binance", binanceBucket,
            "kraken", krakenBucket
        );
    }

    public boolean tryConsume(String exchange) {
        Bucket bucket = buckets.get(exchange.toLowerCase());
        if (bucket == null) {
            throw new IllegalArgumentException("No rate limit configured for exchange: " + exchange);
        }

        boolean consumed = bucket.tryConsume(1);
        if (!consumed) {
            log.warn("Rate limit exceeded for exchange: {}", exchange);
        }
        return consumed;
    }
}
