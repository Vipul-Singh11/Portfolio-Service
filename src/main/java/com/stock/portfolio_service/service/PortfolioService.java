package com.stock.portfolio_service.service;

import com.stock.portfolio_service.dto.PortfolioResponseDto;
import com.stock.portfolio_service.dto.PortfolioSummaryDto;
import com.stock.portfolio_service.dto.TradeEventDto;
import com.stock.portfolio_service.dto.TradeHistoryResponseDto;

import java.util.List;

public interface PortfolioService {

    void processTrade(TradeEventDto trade);

    List<PortfolioResponseDto> getUserPortfolio(Long userId);

    PortfolioSummaryDto getPortfolioSummary(Long userId);

    List<TradeHistoryResponseDto> getTradeHistory(Long userId);

    void reserveShares(
            Long userId,
            String stockSymbol,
            Integer quantity);

    void releaseReservedShares(
            Long userId,
            String stockSymbol,
            Integer quantity);

    void consumeReservedShares(
            Long userId,
            String stockSymbol,
            Integer quantity);
}