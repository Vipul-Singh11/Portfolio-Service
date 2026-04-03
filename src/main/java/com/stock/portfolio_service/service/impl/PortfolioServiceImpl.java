package com.stock.portfolio_service.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stock.portfolio_service.client.StockPriceClient;
import com.stock.portfolio_service.dto.PortfolioResponseDto;
import com.stock.portfolio_service.dto.PortfolioSummaryDto;
import com.stock.portfolio_service.dto.TradeEventDto;
import com.stock.portfolio_service.entity.Portfolio;
import com.stock.portfolio_service.entity.ProcessedTrade;
import com.stock.portfolio_service.exception.InvalidTradeException;
import com.stock.portfolio_service.repository.PortfolioRepository;
import com.stock.portfolio_service.repository.ProcessedTradeRepository;
import com.stock.portfolio_service.service.PortfolioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository repository;
    private final ProcessedTradeRepository processedTradeRepository;
    private final StockPriceClient stockPriceClient;

    @Override
    @Transactional
    public void processTrade(TradeEventDto trade) {

        validateTrade(trade);
        log.info("Processing trade: {}", trade.getTradeId());

        if (processedTradeRepository.existsById(trade.getTradeId())) {
            log.warn("Trade already processed: {}", trade.getTradeId());
            return;
        }

        Long buyerId = trade.getBuyerUserId();
        Long sellerId = trade.getSellerUserId();

        // 🔵 BUYER
        Portfolio buyerPortfolio = repository
                .findByUserIdAndStockSymbol(buyerId, trade.getStockSymbol())
                .orElse(Portfolio.builder()
                        .userId(buyerId)
                        .stockSymbol(trade.getStockSymbol())
                        .quantity(0)
                        .build());

        buyerPortfolio.setQuantity(buyerPortfolio.getQuantity() + trade.getQuantity());
        repository.save(buyerPortfolio);

        // 🔴 SELLER
        Portfolio sellerPortfolio = repository
                .findByUserIdAndStockSymbol(sellerId, trade.getStockSymbol())
                .orElseThrow(() -> new InvalidTradeException("Seller portfolio not found"));

        if (sellerPortfolio.getQuantity() < trade.getQuantity()) {
            throw new InvalidTradeException("Insufficient stocks for seller");
        }

        int updatedQty = sellerPortfolio.getQuantity() - trade.getQuantity();

        if (updatedQty == 0) {
            repository.deleteByUserIdAndStockSymbol(sellerId, trade.getStockSymbol());
        } else {
            sellerPortfolio.setQuantity(updatedQty);
            repository.save(sellerPortfolio);
        }

        processedTradeRepository.save(
                ProcessedTrade.builder()
                        .tradeId(trade.getTradeId())
                        .build()
        );

        log.info("Trade processed successfully: {}", trade.getTradeId());
    }

    @Override
    public List<PortfolioResponseDto> getUserPortfolio(Long userId) {
        return repository.findAllByUserId(userId)
                .stream()
                .map(p -> PortfolioResponseDto.builder()
                        .userId(p.getUserId())
                        .stockSymbol(p.getStockSymbol())
                        .quantity(p.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

   @Override
    public PortfolioSummaryDto getPortfolioSummary(Long userId) {

        List<Portfolio> portfolios = repository.findAllByUserId(userId);

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        List<PortfolioResponseDto> holdings = new java.util.ArrayList<>();

        for (Portfolio p : portfolios) {

            BigDecimal currentPrice = stockPriceClient.getCurrentPrice(p.getStockSymbol());
            BigDecimal value = currentPrice.multiply(BigDecimal.valueOf(p.getQuantity()));

            currentValue = currentValue.add(value);

            holdings.add(
                    PortfolioResponseDto.builder()
                            .userId(p.getUserId())
                            .stockSymbol(p.getStockSymbol())
                            .quantity(p.getQuantity())
                            .build()
            );
        }

        BigDecimal profitLoss = currentValue.subtract(totalInvestment);

        return PortfolioSummaryDto.builder()
                .userId(userId)
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .profitLoss(profitLoss)
                .holdings(holdings)
                .build();
    }

    private void validateTrade(TradeEventDto trade) {

        if (trade == null) {
            throw new InvalidTradeException("Trade cannot be null");
        }

        if (trade.getTradeId() == null) {
            throw new InvalidTradeException("TradeId is required");
        }

        if (trade.getBuyerUserId() == null || trade.getSellerUserId() == null) {
            throw new InvalidTradeException("Buyer/Seller IDs are required");
        }

        if (trade.getQuantity() == null || trade.getQuantity() <= 0) {
            throw new InvalidTradeException("Quantity must be positive");
        }

        if (trade.getStockSymbol() == null || trade.getStockSymbol().isBlank()) {
            throw new InvalidTradeException("Stock symbol is required");
        }
    }
}