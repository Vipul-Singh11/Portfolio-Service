package com.stock.portfolio_service.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.portfolio_service.dto.TradeEventDto;
import com.stock.portfolio_service.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeEventSubscriber implements MessageListener {

    private final PortfolioService portfolioService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        handleMessage(new String(message.getBody(), StandardCharsets.UTF_8));
    }

    public void handleMessage(String body) {
        try {
            TradeEventDto trade = objectMapper.readValue(body, TradeEventDto.class);
            portfolioService.processTrade(trade);
        } catch (Exception e) {
            log.error("Error processing trade event: {}", body, e);
        }
    }
}