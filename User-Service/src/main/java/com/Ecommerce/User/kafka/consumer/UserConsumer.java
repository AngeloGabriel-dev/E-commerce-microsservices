package com.Ecommerce.User.kafka.consumer;

import com.Ecommerce.User.service.UserService;
import com.Ecommerce.common.kafka.event.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConsumer {

    private final UserService userService;

    @KafkaListener(topics = "user-created", groupId = "user-service")
    public void consumeUserCreated(UserCreatedEvent event){
        userService.saveUser(event);
    }

    @KafkaListener(topics = "user-deleted", groupId = "user-service")
    public void consumeUserDeleted(UserDeletedEvent event){
        userService.deleteUser(event);
    }
}
