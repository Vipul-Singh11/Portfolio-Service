package com.stock.portfolio_service.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeEventDto implements Serializable {

    private Long tradeId;
    private Long buyOrderId;
    private Long sellOrderId;

    private Long buyerUserId;
    private Long sellerUserId;

    private String stockSymbol;
    private Integer quantity;
    private BigDecimal executedPrice;

    private LocalDateTime executionTime;
}