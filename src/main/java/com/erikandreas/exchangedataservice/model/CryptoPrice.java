package com.erikandreas.exchangedataservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoPrice implements Serializable {
    private String symbol;
    private BigDecimal price;
    private String exchange;
    private LocalDateTime timestamp;
}
