package com.erikandreas.exchangedataservice.service;

import com.erikandreas.exchangedataservice.model.CryptoPrice;

public interface BlockingPriceService {
    CryptoPrice getPrice(String symbol);
}
