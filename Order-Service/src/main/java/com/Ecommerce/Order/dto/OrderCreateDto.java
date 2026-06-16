package com.Ecommerce.Order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderCreateDto {

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotEmpty(message = "At least one seller order is required")
    @Valid
    private List<SellerOrderDto> sellerOrders;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerOrderDto {

        @NotNull(message = "Seller ID is required")
        private UUID sellerId;

        @NotEmpty(message = "At least one item is required")
        @Valid
        private List<OrderItemDto> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotEmpty(message = "Product name is required")
        private String productName;

        @NotNull(message = "Unit price is required")
        private java.math.BigDecimal unitPrice;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}