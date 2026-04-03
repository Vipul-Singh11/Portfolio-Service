package com.stock.portfolio_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class StockPriceClient {

    private final RedisTemplate<String, Object> redisTemplate;

        public BigDecimal getCurrentPrice(String stockSymbol) {

        String key = "stock:price:" + stockSymbol;

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            // fallback (temporary)
            return BigDecimal.valueOf(100);
        }

        return new BigDecimal(value.toString());
    }
}