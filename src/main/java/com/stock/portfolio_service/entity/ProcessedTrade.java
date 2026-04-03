package com.stock.portfolio_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "processed_trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedTrade {

    @Id
    private Long tradeId;
}