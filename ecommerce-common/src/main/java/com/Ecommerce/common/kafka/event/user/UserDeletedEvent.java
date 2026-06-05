package com.Ecommerce.common.kafka.event.user;

public record UserDeletedEvent(
        Long userId,
        String email
) {}
