package com.Ecommerce.Notification.consumer;

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

    @KafkaListener(topics = "payment-confirmed", groupId = "notification-service")
    public void consumePaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("Received payment-confirmed event for order: {}", event.orderId());
        notificationService.notifyPaymentConfirmed(event, "cliente@email.com", "+5511999999999");
    }

    @KafkaListener(topics = "payment-expired", groupId = "notification-service")
    public void consumePaymentExpired(PaymentExpiredEvent event) {
        log.info("Received payment-expired event for order: {}", event.orderId());
        notificationService.notifyPaymentExpired(event, "cliente@email.com", "+5511999999999");
    }
}