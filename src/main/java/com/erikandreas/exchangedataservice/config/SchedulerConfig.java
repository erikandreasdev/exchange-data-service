package com.erikandreas.exchangedataservice.config;

import com.erikandreas.exchangedataservice.service.DailyAveragePriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private DailyAveragePriceService averagePriceService;

    private static final List<String> SYMBOLS = List.of("BTCUSDT", "ETHUSDT");
    private static final List<String> EXCHANGES = List.of("binance", "kraken");

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void updateAverages() {
        for (String exchange : EXCHANGES) {
            for (String symbol : SYMBOLS) {
                averagePriceService.updateDailyAverage(exchange, symbol);
            }
        }
    }
}