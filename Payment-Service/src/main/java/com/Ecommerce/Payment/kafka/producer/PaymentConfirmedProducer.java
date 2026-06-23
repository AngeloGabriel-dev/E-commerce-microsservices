package com.Ecommerce.Payment.kafka.producer;

import com.Ecommerce.common.kafka.event.payment.PaymentConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentConfirmedProducer {

    private final KafkaTemplate<String, PaymentConfirmedEvent> kafkaTemplate;

    public void send(PaymentConfirmedEvent event) {
        kafkaTemplate.send("payment-confirmed", event);
    }
}