package com.erikandreas.exchangedataservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KrakenTickerDTO {
    private List<String> error;
    private Map<String, KrakenTickerInfo> result;

    @Data
    public static class KrakenTickerInfo {
        @JsonProperty("a")
        private List<String> ask; // [price, whole lot volume, lot volume]

        @JsonProperty("b")
        private List<String> bid; // [price, whole lot volume, lot volume]
    }
}
