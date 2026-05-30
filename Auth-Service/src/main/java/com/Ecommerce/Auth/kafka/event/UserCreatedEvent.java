package com.Ecommerce.Auth.kafka.event;

import com.Ecommerce.Auth.entity.User;

public record UserCreatedEvent(Long userId,
                               String name,
                               String phoneNumber,
                               String cpf,
                               String email,
                               User.Role role) {

}
