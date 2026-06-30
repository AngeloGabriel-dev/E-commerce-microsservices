package com.Ecommerce.Notification.consumer;

import com.Ecommerce.Notification.service.NotificationService;
import com.Ecommerce.common.kafka.event.order.OrderCancelledEvent;
import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-confirmed", groupId = "notification-service")
    public void consumeOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received order-confirmed event: {}", event.orderId());
        // In a real scenario, we would fetch user email/phone from User-Service
        // For now, we use placeholders
        notificationService.notifyOrderConfirmed(event, "cliente@email.com", "+5511999999999");
    }

    @KafkaListener(topics = "order-cancelled", groupId = "notification-service")
    public void consumeOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order-cancelled event: {}", event.orderId());
        notificationService.notifyOrderCancelled(event, "cliente@email.com", "+5511999999999");
    }
}