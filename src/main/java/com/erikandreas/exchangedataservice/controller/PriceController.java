package com.erikandreas.exchangedataservice.controller;

import com.erikandreas.exchangedataservice.model.CryptoPrice;
import com.erikandreas.exchangedataservice.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/prices")
@Tag(name = "Crypto Prices", description = "API endpoints for fetching real-time cryptocurrency prices from different exchanges")
@Slf4j
public class PriceController {

    private final Map<String, BlockingPriceService> blockingServices;
    private final Map<String, ReactivePriceService> reactiveServices;

    public PriceController(
            BinanceRestTemplateService binanceBlockingService,
            KrakenRestTemplateService krakenBlockingService,
            BinanceWebClientService binanceReactiveService,
            KrakenWebClientService krakenReactiveService) {

        this.blockingServices = Map.of(
            "binance", binanceBlockingService,
            "kraken", krakenBlockingService
        );

        this.reactiveServices = Map.of(
            "binance", binanceReactiveService,
            "kraken", krakenReactiveService
        );
    }

    @Operation(
        summary = "Get cryptocurrency price (blocking)",
        description = "Fetches the current price of a cryptocurrency using synchronous REST client. " +
                     "Supports multiple exchanges and trading pairs. Prices are cached to respect rate limits."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved price",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CryptoPrice.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "symbol": "BTCUSDT",
                        "price": 45000.00,
                        "exchange": "Binance",
                        "timestamp": "2024-02-14T10:15:30"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "message": "Unsupported exchange: unknown",
                        "errorCode": "INVALID_INPUT",
                        "timestamp": "2024-02-14T10:15:30"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Exchange service unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/blocking/{exchange}/{symbol}")
    public CryptoPrice getPrice(
            @Parameter(
                description = "Exchange name",
                example = "binance",
                schema = @Schema(allowableValues = {"binance", "kraken"})
            )
            @PathVariable String exchange,
            @Parameter(
                description = "Trading symbol (e.g., BTCUSDT for Bitcoin/USDT pair)",
                example = "BTCUSDT"
            )
            @PathVariable String symbol) {
        BlockingPriceService service = blockingServices.get(exchange.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported exchange: " + exchange);
        }
        return service.getPrice(symbol);
    }

    @Operation(
        summary = "Get cryptocurrency price (reactive)",
        description = "Fetches the current price of a cryptocurrency using non-blocking WebClient. " +
                     "Provides better performance under high load. Prices are cached to respect rate limits."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved price",
            content = @Content(schema = @Schema(implementation = CryptoPrice.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Exchange service unavailable",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/reactive/{exchange}/{symbol}")
    public Mono<CryptoPrice> getPriceReactive(
            @Parameter(
                description = "Exchange name",
                example = "binance",
                schema = @Schema(allowableValues = {"binance", "kraken"})
            )
            @PathVariable String exchange,
            @Parameter(
                description = "Trading symbol (e.g., BTCUSDT for Bitcoin/USDT pair)",
                example = "BTCUSDT"
            )
            @PathVariable String symbol) {
        ReactivePriceService service = reactiveServices.get(exchange.toLowerCase());
        if (service == null) {
            return Mono.error(new IllegalArgumentException("Unsupported exchange: " + exchange));
        }
        return service.getPrice(symbol);
    }

    @Operation(
        summary = "List available exchanges",
        description = "Returns a list of supported exchanges for both blocking and reactive endpoints. " +
                     "Use these values in the exchange parameter of price endpoints."
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of available exchanges",
        content = @Content(
            examples = @ExampleObject(
                value = """
                {
                    "blocking": ["binance", "kraken"],
                    "reactive": ["binance", "kraken"]
                }
                """
            )
        )
    )
    @GetMapping("/exchanges")
    public Map<String, List<String>> getAvailableExchanges() {
        return Map.of(
            "blocking", new ArrayList<>(blockingServices.keySet()),
            "reactive", new ArrayList<>(reactiveServices.keySet())
        );
    }
}