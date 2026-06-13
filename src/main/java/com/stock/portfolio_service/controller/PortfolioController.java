package com.stock.portfolio_service.controller;

import com.stock.portfolio_service.dto.PortfolioResponseDto;
import com.stock.portfolio_service.dto.PortfolioSummaryDto;
import com.stock.portfolio_service.dto.TradeEventDto;
import com.stock.portfolio_service.service.PortfolioService;
import com.stock.portfolio_service.dto.TradeHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService service;

    @GetMapping("/{userId}")
    public ResponseEntity<List<PortfolioResponseDto>> getPortfolio(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUserPortfolio(userId));
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<PortfolioSummaryDto> getSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getPortfolioSummary(userId));
    }

    @PostMapping("/trade")
    public ResponseEntity<String> processTrade(@RequestBody TradeEventDto trade) {
        service.processTrade(trade);
        return ResponseEntity.ok("Trade processed");
    }

    @GetMapping("/trades/{userId}")
    public ResponseEntity<List<TradeHistoryResponseDto>> getTradeHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                service.getTradeHistory(userId));
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveShares(
            @RequestParam Long userId,
            @RequestParam String stockSymbol,
            @RequestParam Integer quantity) {

        service.reserveShares(
                userId,
                stockSymbol,
                quantity);

        return ResponseEntity.ok(
                "Shares reserved");
    }

    @PostMapping("/release")
    public ResponseEntity<String> releaseReservedShares(
            @RequestParam Long userId,
            @RequestParam String stockSymbol,
            @RequestParam Integer quantity) {

        service.releaseReservedShares(
                userId,
                stockSymbol,
                quantity);

        return ResponseEntity.ok(
                "Reserved shares released");
    }

    @PostMapping("/consume")
    public ResponseEntity<String> consumeReservedShares(
            @RequestParam Long userId,
            @RequestParam String stockSymbol,
            @RequestParam Integer quantity) {

        service.consumeReservedShares(
                userId,
                stockSymbol,
                quantity);

        return ResponseEntity.ok(
                "Reserved shares consumed");
    }
}