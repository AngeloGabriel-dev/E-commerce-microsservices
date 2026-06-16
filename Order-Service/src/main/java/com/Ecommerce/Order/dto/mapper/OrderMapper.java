package com.Ecommerce.Order.dto.mapper;

import com.Ecommerce.Order.dto.OrderCreateDto;
import com.Ecommerce.Order.dto.OrderResponseDto;
import com.Ecommerce.Order.entity.Order;
import com.Ecommerce.Order.entity.OrderItem;
import com.Ecommerce.Order.entity.SellerOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order toOrder(OrderCreateDto dto) {
        Order order = Order.builder()
                .clientId(dto.getClientId())
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .totalPrice(BigDecimal.ZERO)
                .sellerOrders(new ArrayList<>())
                .build();

        List<SellerOrder> sellerOrders = dto.getSellerOrders().stream()
                .map(sellerOrderDto -> {
                    SellerOrder sellerOrder = SellerOrder.builder()
                            .order(order)
                            .sellerId(sellerOrderDto.getSellerId())
                            .status(SellerOrder.SellerOrderStatus.PENDING_PAYMENT)
                            .subTotal(BigDecimal.ZERO)
                            .items(new ArrayList<>())
                            .build();

                    List<OrderItem> items = sellerOrderDto.getItems().stream()
                            .map(itemDto -> {
                                OrderItem item = OrderItem.builder()
                                        .sellerOrder(sellerOrder)
                                        .productId(itemDto.getProductId())
                                        .productName(itemDto.getProductName())
                                        .unitPrice(itemDto.getUnitPrice())
                                        .quantity(itemDto.getQuantity())
                                        .build();
                                return item;
                            })
                            .collect(Collectors.toList());

                    sellerOrder.setItems(items);

                    BigDecimal subTotal = items.stream()
                            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    sellerOrder.setSubTotal(subTotal);

                    return sellerOrder;
                })
                .collect(Collectors.toList());

        order.setSellerOrders(sellerOrders);

        BigDecimal totalPrice = sellerOrders.stream()
                .map(SellerOrder::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);

        return order;
    }

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