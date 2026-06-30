package com.Ecommerce.Order.kafka.producer;

import com.Ecommerce.common.kafka.event.order.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCancelledProducer {

    private final KafkaTemplate<String, OrderCancelledEvent> kafkaTemplate;

    public void send(OrderCancelledEvent event) {
        kafkaTemplate.send("order-cancelled", event);
    }
}