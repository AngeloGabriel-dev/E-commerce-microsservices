package com.Ecommerce.Notification.consumer;

import com.Ecommerce.Notification.client.UserServiceClient;
import com.Ecommerce.Notification.dto.UserContactInfoDto;
import com.Ecommerce.Notification.service.NotificationService;
import com.Ecommerce.common.kafka.event.order.OrderCancelledEvent;
import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;
    private final UserServiceClient userServiceClient;

    @KafkaListener(topics = "order-confirmed", groupId = "notification-service")
    public void consumeOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received order-confirmed event: {}", event.orderId());
        // Fetch real user contact info from User-Service
        try {
            UserContactInfoDto contactInfo = userServiceClient.getUserContactInfo(event.clientId());
            notificationService.notifyOrderConfirmed(event, contactInfo.getEmail(), contactInfo.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to fetch user contact info for client: {}", event.clientId(), e);
            // Fallback to placeholder in case of error
            notificationService.notifyOrderConfirmed(event, "cliente@email.com", "+5511999999999");
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "notification-service")
    public void consumeOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order-cancelled event: {}", event.orderId());
        
        // Fetch real user contact info from User-Service
        try {
            UserContactInfoDto contactInfo = userServiceClient.getUserContactInfo(event.clientId());
            notificationService.notifyOrderCancelled(event, contactInfo.getEmail(), contactInfo.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to fetch user contact info for client: {}", event.clientId(), e);
            // Fallback to placeholder in case of error
            notificationService.notifyOrderCancelled(event, "cliente@email.com", "+5511999999999");
        }
    }
}
