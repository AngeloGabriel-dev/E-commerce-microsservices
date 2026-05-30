package com.Ecommerce.Auth.kafka.producer;

import com.Ecommerce.Auth.kafka.event.UserCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedProducer {
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public UserCreatedProducer(
            KafkaTemplate<String, UserCreatedEvent> kafkaTemplate
    ){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(UserCreatedEvent event){
        kafkaTemplate.send("user-created", event);
    }
}
