package com.stock.portfolio_service.repository;

import com.stock.portfolio_service.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUserIdAndStockSymbol(Long userId, String stockSymbol);

    List<Portfolio> findAllByUserId(Long userId);

    void deleteByUserIdAndStockSymbol(Long userId, String stockSymbol);

    boolean existsByUserIdAndStockSymbol(Long userId, String stockSymbol);
}