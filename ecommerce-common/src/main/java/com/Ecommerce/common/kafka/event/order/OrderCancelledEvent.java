package com.Ecommerce.common.kafka.event.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        UUID clientId,
        BigDecimal totalPrice,
        String reason
) {}