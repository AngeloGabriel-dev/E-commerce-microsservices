package com.Ecommerce.Order.dto.mapper;

import com.Ecommerce.Order.dto.OrderResponseDto;
import com.Ecommerce.Order.entity.Order;
import com.Ecommerce.Order.entity.OrderItem;
import com.Ecommerce.Order.entity.SellerOrder;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponseDto toDto(Order order) {
        List<OrderResponseDto.SellerOrderResponseDto> sellerOrderDtos = order.getSellerOrders().stream()
                .map(sellerOrder -> {
                    List<OrderResponseDto.OrderItemResponseDto> itemDtos = sellerOrder.getItems().stream()
                            .map(item -> OrderResponseDto.OrderItemResponseDto.builder()
                                    .id(item.getId())
                                    .productId(item.getProductId())
                                    .productName(item.getProductName())
                                    .unitPrice(item.getUnitPrice())
                                    .quantity(item.getQuantity())
                                    .createdAt(item.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    return OrderResponseDto.SellerOrderResponseDto.builder()
                            .id(sellerOrder.getId())
                            .sellerId(sellerOrder.getSellerId())
                            .status(sellerOrder.getStatus().name())
                            .subTotal(sellerOrder.getSubTotal())
                            .items(itemDtos)
                            .createdAt(sellerOrder.getCreatedAt())
                            .updatedAt(sellerOrder.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .id(order.getId())
                .clientId(order.getClientId())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .sellerOrders(sellerOrderDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static List<OrderResponseDto> toListDto(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }
}