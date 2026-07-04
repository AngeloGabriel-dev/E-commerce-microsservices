package com.Ecommerce.Payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.order-service.url}")
    private String orderServiceUrl;

    public OrderResponseDto getOrderById(UUID orderId) {
        String url = orderServiceUrl + "/api/v1/orders/" + orderId;
        log.info("Fetching order from Order-Service: {}", url);
        return restTemplate.getForObject(url, OrderResponseDto.class);
    }
}