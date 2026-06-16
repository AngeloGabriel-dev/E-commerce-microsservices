package com.Ecommerce.Catalog.kafka.consumer;

import com.Ecommerce.Catalog.service.CatalogService;
import com.Ecommerce.common.kafka.event.order.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmedConsumer {

    private final CatalogService catalogService;

    @KafkaListener(topics = "order-confirmed", groupId = "catalog-service")
    public void consumeOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received order confirmed event for order: {}. Deducting stock.", event.orderId());
        catalogService.deductStock(event);
    }
}