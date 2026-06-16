package com.Ecommerce.common.kafka.event.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentConfirmedEvent(
        UUID orderId,
        UUID clientId,
        BigDecimal totalPrice,
        List<SellerOrderEvent> sellerOrders
) {
    public record SellerOrderEvent(
            UUID sellerOrderId,
            UUID sellerId,
            BigDecimal subTotal,
            List<OrderItemEvent> items
    ) {
        public record OrderItemEvent(
                UUID productId,
                String productName,
                BigDecimal unitPrice,
                Integer quantity
        ) {}
    }
}