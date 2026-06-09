package com.stock.portfolio_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeHistory {

    @Id
    private Long tradeId;

    private Long buyOrderId;

    private Long sellOrderId;

    private Long buyerUserId;

    private Long sellerUserId;

    private String stockSymbol;

    private Integer quantity;

    private BigDecimal price;

    private LocalDateTime executionTime;
}