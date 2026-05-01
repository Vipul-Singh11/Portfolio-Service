package com.stock.portfolio_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${services.user.base-url}")
    private String baseUrl;

    public void debit(Long userId, BigDecimal amount) {
        String url = String.format(
                "%s/api/users/debit?userId=%d&amount=%s",
                baseUrl, userId, amount
        );

        ResponseEntity<Void> response =
                restTemplate.postForEntity(url, null, Void.class);

        validateResponse(response);
    }

    public void credit(Long userId, BigDecimal amount) {
        String url = String.format(
                "%s/api/users/credit?userId=%d&amount=%s",
                baseUrl, userId, amount
        );

        ResponseEntity<Void> response =
                restTemplate.postForEntity(url, null, Void.class);

        validateResponse(response);
    }

    private void validateResponse(ResponseEntity<?> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    "User Service call failed: " + response.getStatusCode()
            );
        }
    }
}
