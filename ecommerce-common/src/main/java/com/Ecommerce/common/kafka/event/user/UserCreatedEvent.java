package com.Ecommerce.common.kafka.event.user;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String name,
        String phoneNumber,
        String cpf,
        String email,
        String role
) {}
