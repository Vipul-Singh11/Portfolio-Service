package com.stock.portfolio_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeHistoryResponseDto {

    private Long tradeId;
    private Long buyerUserId;
    private Long sellerUserId;
    private String stockSymbol;
    private Integer quantity;
    private BigDecimal price;
    private LocalDateTime executionTime;
}