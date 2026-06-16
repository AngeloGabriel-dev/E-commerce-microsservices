package com.Ecommerce.Order.kafka.consumer;

import com.Ecommerce.Order.service.OrderService;
import com.Ecommerce.common.kafka.event.payment.PaymentConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConfirmedConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-confirmed", groupId = "order-service")
    public void consumePaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("Received payment confirmed event for order: {}", event.orderId());
        orderService.confirmOrder(event);
    }
}