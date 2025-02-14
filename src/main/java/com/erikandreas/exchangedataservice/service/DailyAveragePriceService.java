package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.model.DailyPriceAverage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;

@Service
@Slf4j
public class DailyAveragePriceService {

    private final StringRedisTemplate redisTemplate;
    private final BinanceRestTemplateService binanceService;
    private final KrakenRestTemplateService krakenService;
    private final ObjectMapper objectMapper;

    public DailyAveragePriceService(
            StringRedisTemplate redisTemplate,
            BinanceRestTemplateService binanceService,
            KrakenRestTemplateService krakenService,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.binanceService = binanceService;
        this.krakenService = krakenService;
        this.objectMapper = objectMapper;
    }

    private String buildRedisKey(String exchange, String symbol, LocalDate date) {
        return String.format("daily:avg:%s:%s:%s", exchange, symbol, date);
    }

    public void updateDailyAverage(String exchange, String symbol) {
        try {
            LocalDate today = LocalDate.now();
            String redisKey = buildRedisKey(exchange, symbol, today);

            // Try to get existing average
            DailyPriceAverage currentAvg = getCurrentAverage(redisKey);

            // Get current price
            CryptoPrice currentPrice = fetchCurrentPrice(exchange, symbol);

            // Calculate new average
            DailyPriceAverage newAvg;
            if (currentAvg == null) {
                newAvg = DailyPriceAverage.builder()
                    .symbol(symbol)
                    .exchange(exchange)
                    .averagePrice(currentPrice.getPrice())
                    .date(today)
                    .numberOfUpdates(1)
                    .build();
            } else {
                BigDecimal newAveragePrice = calculateNewAverage(
                    currentAvg.getAveragePrice(),
                    currentPrice.getPrice(),
                    currentAvg.getNumberOfUpdates()
                );

                newAvg = DailyPriceAverage.builder()
                    .symbol(symbol)
                    .exchange(exchange)
                    .averagePrice(newAveragePrice)
                    .date(today)
                    .numberOfUpdates(currentAvg.getNumberOfUpdates() + 1)
                    .build();
            }

            // Store updated average
            storeDailyAverage(redisKey, newAvg);

            log.info("Updated daily average for {}/{}: {}", exchange, symbol, newAvg.getAveragePrice());

        } catch (Exception e) {
            log.error("Error updating daily average for {}/{}: {}", exchange, symbol, e.getMessage());
        }
    }

    private DailyPriceAverage getCurrentAverage(String redisKey) {
        try {
            String storedJson = redisTemplate.opsForValue().get(redisKey);
            if (storedJson == null) {
                return null;
            }
            return objectMapper.readValue(storedJson, DailyPriceAverage.class);
        } catch (Exception e) {
            log.error("Error reading daily average from Redis: {}", e.getMessage());
            return null;
        }
    }

    private void storeDailyAverage(String redisKey, DailyPriceAverage avg) {
        try {
            String json = objectMapper.writeValueAsString(avg);
            redisTemplate.opsForValue().set(redisKey, json);
            // Set expiry to 2 days to ensure we keep today's data through tomorrow
            redisTemplate.expire(redisKey, Duration.ofDays(2));
        } catch (Exception e) {
            log.error("Error storing daily average in Redis: {}", e.getMessage());
        }
    }

    private CryptoPrice fetchCurrentPrice(String exchange, String symbol) {
        return switch (exchange.toLowerCase()) {
            case "binance" -> binanceService.getPrice(symbol);
            case "kraken" -> krakenService.getPrice(symbol);
            default -> throw new IllegalArgumentException("Unsupported exchange: " + exchange);
        };
    }

    private BigDecimal calculateNewAverage(BigDecimal currentAvg, BigDecimal newPrice, int currentCount) {
        return currentAvg
            .multiply(BigDecimal.valueOf(currentCount))
            .add(newPrice)
            .divide(BigDecimal.valueOf(currentCount + 1), RoundingMode.HALF_UP);
    }

    public DailyPriceAverage getDailyAverage(String exchange, String symbol, LocalDate date) {
        String redisKey = buildRedisKey(exchange, symbol, date);
        return getCurrentAverage(redisKey);
    }
}
