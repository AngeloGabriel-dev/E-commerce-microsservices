package com.Ecommerce.common.kafka.event.user;

public record UserCreatedEvent(
        Long userId,
        String name,
        String phoneNumber,
        String cpf,
        String email,
        String role
) {}
