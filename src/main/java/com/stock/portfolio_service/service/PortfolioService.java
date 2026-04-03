package com.stock.portfolio_service.service;

import com.stock.portfolio_service.dto.PortfolioResponseDto;
import com.stock.portfolio_service.dto.PortfolioSummaryDto;
import com.stock.portfolio_service.dto.TradeEventDto;

import java.util.List;

public interface PortfolioService {

    void processTrade(TradeEventDto trade);

    List<PortfolioResponseDto> getUserPortfolio(Long userId);
    PortfolioSummaryDto getPortfolioSummary(Long userId);
}