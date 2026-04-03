package com.stock.portfolio_service.repository;

import com.stock.portfolio_service.entity.ProcessedTrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedTradeRepository extends JpaRepository<ProcessedTrade, Long> {
}