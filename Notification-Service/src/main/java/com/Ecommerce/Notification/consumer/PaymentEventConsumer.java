package com.Ecommerce.Notification.consumer;

import com.Ecommerce.Notification.client.UserServiceClient;
import com.Ecommerce.Notification.dto.UserContactInfoDto;
import com.Ecommerce.Notification.service.NotificationService;
import com.Ecommerce.common.kafka.event.payment.PaymentConfirmedEvent;
import com.Ecommerce.common.kafka.event.payment.PaymentExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final UserServiceClient userServiceClient;

    @KafkaListener(topics = "payment-confirmed", groupId = "notification-service")
    public void consumePaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("Received payment-confirmed event for order: {}", event.orderId());
        
        // Fetch real user contact info from User-Service
        try {
            UserContactInfoDto contactInfo = userServiceClient.getUserContactInfo(event.clientId());
            notificationService.notifyPaymentConfirmed(event, contactInfo.getEmail(), contactInfo.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to fetch user contact info for client: {}", event.clientId(), e);
            // Fallback to placeholder in case of error
            notificationService.notifyPaymentConfirmed(event, "cliente@email.com", "+5511999999999");
        }
    }

    @KafkaListener(topics = "payment-expired", groupId = "notification-service")
    public void consumePaymentExpired(PaymentExpiredEvent event) {
        log.info("Received payment-expired event for order: {}", event.orderId());
        
        // Fetch real user contact info from User-Service
        try {
            UserContactInfoDto contactInfo = userServiceClient.getUserContactInfo(event.clientId());
            notificationService.notifyPaymentExpired(event, contactInfo.getEmail(), contactInfo.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to fetch user contact info for client: {}", event.clientId(), e);
            // Fallback to placeholder in case of error
            notificationService.notifyPaymentExpired(event, "cliente@email.com", "+5511999999999");
        }
    }
}
