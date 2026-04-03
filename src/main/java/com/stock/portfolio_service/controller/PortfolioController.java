package com.stock.portfolio_service.controller;

import com.stock.portfolio_service.dto.PortfolioResponseDto;
import com.stock.portfolio_service.dto.PortfolioSummaryDto;
import com.stock.portfolio_service.dto.TradeEventDto;
import com.stock.portfolio_service.service.PortfolioService;
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
}