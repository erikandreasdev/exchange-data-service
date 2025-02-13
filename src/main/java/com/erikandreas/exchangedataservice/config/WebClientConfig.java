package com.erikandreas.exchangedataservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient binanceWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.binance.com/api/v3")
            .build();
    }
}