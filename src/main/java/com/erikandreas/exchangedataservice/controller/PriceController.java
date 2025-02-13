package com.erikandreas.exchangedataservice.controller;

import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.service.BinanceRestTemplateService;
import com.erikandreas.exchangedataservice.service.BinanceWebClientService;
import com.erikandreas.exchangedataservice.service.BlockingPriceService;
import com.erikandreas.exchangedataservice.service.ReactivePriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final BlockingPriceService restTemplateService;
    private final ReactivePriceService webClientService;

    public PriceController(
            BinanceRestTemplateService restTemplateService,
            BinanceWebClientService webClientService) {
        this.restTemplateService = restTemplateService;
        this.webClientService = webClientService;
    }

    @GetMapping("/blocking/{symbol}")
    public CryptoPrice getPriceBlocking(@PathVariable String symbol) {
        return restTemplateService.getPrice(symbol);
    }

    @GetMapping("/reactive/{symbol}")
    public Mono<CryptoPrice> getPriceReactive(@PathVariable String symbol) {
        return webClientService.getPrice(symbol);
    }
}
