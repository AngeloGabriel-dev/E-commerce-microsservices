package com.Ecommerce.Order.kafka.producer;

import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConfirmedProducer {

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public void send(OrderConfirmedEvent event) {
        kafkaTemplate.send("order-confirmed", event);
    }
}