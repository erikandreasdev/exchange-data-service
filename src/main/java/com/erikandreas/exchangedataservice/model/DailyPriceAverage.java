package com.erikandreas.exchangedataservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPriceAverage {
    private String symbol;
    private String exchange;
    private BigDecimal averagePrice;
    private LocalDate date;
    private int numberOfUpdates;
}
