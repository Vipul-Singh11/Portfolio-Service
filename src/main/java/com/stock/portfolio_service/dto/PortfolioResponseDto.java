package com.stock.portfolio_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponseDto {

    private Long userId;
    private String stockSymbol;
    private Integer quantity;
}