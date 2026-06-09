package com.stock.portfolio_service.repository;

import com.stock.portfolio_service.entity.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeHistoryRepository
        extends JpaRepository<TradeHistory, Long> {

    List<TradeHistory> findByBuyerUserIdOrSellerUserId(
            Long buyerUserId,
            Long sellerUserId);

    List<TradeHistory> findByBuyerUserIdOrSellerUserIdOrderByExecutionTimeDesc(
        Long buyerUserId,
        Long sellerUserId);
}