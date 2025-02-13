package com.erikandreas.exchangedataservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${exchange.binance.base-url}")
    private String binanceBaseUrl;

    @Value("${exchange.kraken.base-url}")
    private String krakenBaseUrl;

    @Bean
    public WebClient binanceWebClient() {
        return WebClient.builder()
            .baseUrl(binanceBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean
    public WebClient krakenWebClient() {
        return WebClient.builder()
            .baseUrl(krakenBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}