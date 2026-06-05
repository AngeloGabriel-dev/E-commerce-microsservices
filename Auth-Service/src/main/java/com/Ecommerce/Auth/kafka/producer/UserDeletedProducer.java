package com.Ecommerce.Auth.kafka.producer;

import com.Ecommerce.common.kafka.event.user.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDeletedProducer {

    private final KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;

    public void send(UserDeletedEvent event){
        kafkaTemplate.send("user-deleted", event);
    }
}
