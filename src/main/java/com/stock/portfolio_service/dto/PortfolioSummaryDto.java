package com.stock.portfolio_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSummaryDto {

    private Long userId;

    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;

    private List<PortfolioResponseDto> holdings;
}