package com.Ecommerce.User.kafka.consumer;

import com.Ecommerce.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConsumer {

    private final UserService userService;

}