package com.stock.portfolio_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.user.base-url}")
    private String baseUrl;

    public void deductBalance(Long userId, Double amount) {
        String url = String.format("%s/deduct?userId=%d&amount=%f", baseUrl, userId, amount);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);
        validateResponse(response);
    }

    public void addBalance(Long userId, Double amount) {
        String url = String.format("%s/add?userId=%d&amount=%f", baseUrl, userId, amount);
        ResponseEntity<Void> response = restTemplate.postForEntity(url, null, Void.class);
        validateResponse(response);
    }

    private void validateResponse(ResponseEntity<?> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("User Service call failed with status: " + response.getStatusCode());
        }
    }
}