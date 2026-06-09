package com.Ecommerce.common.kafka.event.user;

import java.util.UUID;

public record UserDeletedEvent(
        UUID userId,
        String email
) {}
