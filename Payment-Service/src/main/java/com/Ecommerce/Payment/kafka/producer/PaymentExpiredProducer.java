package com.Ecommerce.Payment.kafka.producer;

import com.Ecommerce.common.kafka.event.payment.PaymentExpiredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentExpiredProducer {

    private final KafkaTemplate<String, PaymentExpiredEvent> kafkaTemplate;

    public void send(PaymentExpiredEvent event) {
        kafkaTemplate.send("payment-expired", event);
    }
}