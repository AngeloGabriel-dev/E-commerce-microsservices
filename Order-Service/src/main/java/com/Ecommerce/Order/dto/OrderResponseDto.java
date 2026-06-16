package com.Ecommerce.Order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private UUID id;
    private UUID clientId;
    private String status;
    private BigDecimal totalPrice;
    private List<SellerOrderResponseDto> sellerOrders;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerOrderResponseDto {
        private UUID id;
        private UUID sellerId;
        private String status;
        private BigDecimal subTotal;
        private List<OrderItemResponseDto> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponseDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private LocalDateTime createdAt;
    }
}