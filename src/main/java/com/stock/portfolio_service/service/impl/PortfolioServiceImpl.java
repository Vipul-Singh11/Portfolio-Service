package com.stock.portfolio_service.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stock.portfolio_service.client.StockPriceClient;
import com.stock.portfolio_service.client.UserClient;
import com.stock.portfolio_service.dto.PortfolioResponseDto;
import com.stock.portfolio_service.dto.PortfolioSummaryDto;
import com.stock.portfolio_service.dto.TradeEventDto;
import com.stock.portfolio_service.entity.Portfolio;
import com.stock.portfolio_service.entity.ProcessedTrade;
import com.stock.portfolio_service.exception.InvalidTradeException;
import com.stock.portfolio_service.repository.PortfolioRepository;
import com.stock.portfolio_service.repository.ProcessedTradeRepository;
import com.stock.portfolio_service.service.PortfolioService;
import com.stock.portfolio_service.entity.TradeHistory;
import com.stock.portfolio_service.repository.TradeHistoryRepository;
import com.stock.portfolio_service.dto.TradeHistoryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository repository;
    private final ProcessedTradeRepository processedTradeRepository;
    private final StockPriceClient stockPriceClient;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final UserClient userClient;

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

        BigDecimal totalAmount = trade.getPrice()
                .multiply(BigDecimal.valueOf(trade.getQuantity()));

        userClient.debit(buyerId, totalAmount);

        userClient.credit(sellerId, totalAmount);
        Portfolio buyerPortfolio = repository
                .findByUserIdAndStockSymbol(buyerId, trade.getStockSymbol())
                .orElse(Portfolio.builder()
                .userId(buyerId)
                .stockSymbol(trade.getStockSymbol())
                .quantity(0)
                .reservedQuantity(0)
                .build());

        buyerPortfolio.setQuantity(buyerPortfolio.getQuantity() + trade.getQuantity());
        repository.save(buyerPortfolio);

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

        tradeHistoryRepository.save(
            TradeHistory.builder()
                    .tradeId(trade.getTradeId())
                    .buyOrderId(trade.getBuyOrderId())
                    .sellOrderId(trade.getSellOrderId())
                    .buyerUserId(trade.getBuyerUserId())
                    .sellerUserId(trade.getSellerUserId())
                    .stockSymbol(trade.getStockSymbol())
                    .quantity(trade.getQuantity())
                    .price(trade.getPrice())
                    .executionTime(trade.getExecutionTime())
                    .build()
        );

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
                        .reservedQuantity(p.getReservedQuantity())
                        .availableQuantity(
                                p.getQuantity() - p.getReservedQuantity())
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
                                .reservedQuantity(p.getReservedQuantity())
                                .availableQuantity(p.getQuantity() - p.getReservedQuantity())
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

        if (trade.getQuantity() <= 0) {
            throw new InvalidTradeException("Quantity must be positive");
        }

        if (trade.getStockSymbol() == null || trade.getStockSymbol().isBlank()) {
            throw new InvalidTradeException("Stock symbol is required");
        }
    }

    @Override
    public List<TradeHistoryResponseDto> getTradeHistory(Long userId) {

        return tradeHistoryRepository
                .findByBuyerUserIdOrSellerUserIdOrderByExecutionTimeDesc(
                        userId,
                        userId)
                .stream()
                .map(trade ->
                        TradeHistoryResponseDto.builder()
                                .tradeId(trade.getTradeId())
                                .buyerUserId(trade.getBuyerUserId())
                                .sellerUserId(trade.getSellerUserId())
                                .stockSymbol(trade.getStockSymbol())
                                .quantity(trade.getQuantity())
                                .price(trade.getPrice())
                                .executionTime(trade.getExecutionTime())
                                .build())
                .toList();
    }

    @Override
    public void reserveShares(
            Long userId,
            String stockSymbol,
            Integer quantity) {

        Portfolio portfolio = repository
                .findByUserIdAndStockSymbol(
                        userId,
                        stockSymbol)
                .orElseThrow(() ->
                        new InvalidTradeException(
                                "Portfolio not found"));

        int availableQuantity =
                portfolio.getQuantity()
                        - portfolio.getReservedQuantity();

        if (availableQuantity < quantity) {

            throw new InvalidTradeException(
                    "Insufficient available shares");
        }

        portfolio.setReservedQuantity(
                portfolio.getReservedQuantity() + quantity);

        repository.save(portfolio);
    }
    
    @Override
    public void releaseReservedShares(
            Long userId,
            String stockSymbol,
            Integer quantity) {

        Portfolio portfolio = repository
                .findByUserIdAndStockSymbol(
                        userId,
                        stockSymbol)
                .orElseThrow(() ->
                        new InvalidTradeException(
                                "Portfolio not found"));

        if (portfolio.getReservedQuantity() < quantity) {

            throw new InvalidTradeException(
                    "Reserved quantity cannot become negative");
        }

        portfolio.setReservedQuantity(
                portfolio.getReservedQuantity() - quantity);

        repository.save(portfolio);
    }

    @Override
    public void consumeReservedShares(
            Long userId,
            String stockSymbol,
            Integer quantity) {

        Portfolio portfolio = repository
                .findByUserIdAndStockSymbol(
                        userId,
                        stockSymbol)
                .orElseThrow(() ->
                        new InvalidTradeException(
                                "Portfolio not found"));

        if (portfolio.getReservedQuantity() < quantity) {

            throw new InvalidTradeException(
                    "Insufficient reserved shares");
        }

        if (portfolio.getQuantity() < quantity) {

            throw new InvalidTradeException(
                    "Insufficient shares");
        }

        portfolio.setReservedQuantity(
                portfolio.getReservedQuantity() - quantity);

        portfolio.setQuantity(
                portfolio.getQuantity() - quantity);

        repository.save(portfolio);
    }
}
