package com.Ecommerce.common.kafka.event.order;

import java.util.List;
import java.util.UUID;

public record OrderConfirmedEvent(
        UUID orderId,
        List<ProductStockItem> products
) {
    public record ProductStockItem(
            UUID productId,
            Integer quantity
    ) {}
}