package com.Ecommerce.common.kafka.event.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentExpiredEvent(
        UUID orderId,
        UUID clientId,
        BigDecimal totalPrice
) {}