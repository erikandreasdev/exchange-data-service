package com.erikandreas.exchangedataservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cryptocurrency price information")
public class CryptoPrice {
    @Schema(description = "Trading symbol", example = "BTCUSDT")
    private String symbol;

    @Schema(description = "Current price", example = "45000.00")
    private BigDecimal price;

    @Schema(description = "Exchange name", example = "Binance")
    private String exchange;

    @Schema(description = "Timestamp of the price", example = "2024-02-13T10:15:30")
    private LocalDateTime timestamp;
}
