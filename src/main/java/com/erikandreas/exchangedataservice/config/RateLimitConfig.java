package com.erikandreas.exchangedataservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket binanceBucket() {
        // Binance allows 1200 requests per minute
        Bandwidth limit = Bandwidth.simple(20, Duration.ofMinutes(1));
        return Bucket
                .builder()
                .addLimit(limit)
                .build();
    }

    @Bean
    public Bucket krakenBucket() {
        // Kraken allows 1 request per second
        Bandwidth limit = Bandwidth.simple(1, Duration.ofSeconds(1));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
