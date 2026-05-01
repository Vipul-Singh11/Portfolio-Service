package com.stock.portfolio_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceClient {

    private final RedisTemplate<String, Object> redisTemplate;

    public BigDecimal getCurrentPrice(String stockSymbol) {

        String key = "stock:price:" + stockSymbol;

        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                log.warn("⚠️ Price not found in Redis for {}. Using fallback.", stockSymbol);
                return getFallbackPrice(stockSymbol);
            }

            return new BigDecimal(value.toString());

        } catch (Exception e) {
            log.error("❌ Error fetching stock price from Redis for {}: {}", stockSymbol, e.getMessage());
            return getFallbackPrice(stockSymbol);
        }
    }

    private BigDecimal getFallbackPrice(String stockSymbol) {
        return BigDecimal.valueOf(100);
    }
}
