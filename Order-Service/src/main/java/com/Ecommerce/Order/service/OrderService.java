package com.Ecommerce.Order.service;

import com.Ecommerce.Order.client.CatalogServiceClient;
import com.Ecommerce.Order.client.ProductResponseDto;
import com.Ecommerce.Order.dto.OrderCreateDto;
import com.Ecommerce.Order.dto.OrderResponseDto;
import com.Ecommerce.Order.dto.mapper.OrderMapper;
import com.Ecommerce.Order.entity.Order;
import com.Ecommerce.Order.entity.OrderItem;
import com.Ecommerce.Order.entity.SellerOrder;
import com.Ecommerce.Order.exception.OrderNotFoundException;
import com.Ecommerce.Order.kafka.producer.OrderConfirmedProducer;
import com.Ecommerce.Order.repository.OrderRepository;
import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import com.Ecommerce.common.kafka.event.payment.PaymentConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderConfirmedProducer orderConfirmedProducer;
    private final CatalogServiceClient catalogServiceClient;

    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto, UUID clientId) {
        log.info("Creating order for client: {}", clientId);

        // Fetch all products from catalog
        List<UUID> productIds = dto.getItems().stream()
                .map(OrderCreateDto.OrderItemDto::getProductId)
                .toList();
        Map<UUID, ProductResponseDto> productMap = catalogServiceClient.fetchProducts(productIds);

        // Group items by sellerId (obtained from catalog product data)
        Map<UUID, List<OrderCreateDto.OrderItemDto>> itemsBySeller = dto.getItems().stream()
                .collect(Collectors.groupingBy(
                        item -> productMap.get(item.getProductId()).getSellerId()
                ));

        // Build order
        Order order = Order.builder()
                .clientId(clientId)
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .totalPrice(BigDecimal.ZERO)
                .sellerOrders(new ArrayList<>())
                .build();

        List<SellerOrder> sellerOrders = itemsBySeller.entrySet().stream()
                .map(entry -> {
                    UUID sellerId = entry.getKey();
                    List<OrderCreateDto.OrderItemDto> sellerItems = entry.getValue();

                    SellerOrder sellerOrder = SellerOrder.builder()
                            .order(order)
                            .sellerId(sellerId)
                            .status(SellerOrder.SellerOrderStatus.PENDING_PAYMENT)
                            .subTotal(BigDecimal.ZERO)
                            .items(new ArrayList<>())
                            .build();

                    List<OrderItem> orderItems = sellerItems.stream()
                            .map(itemDto -> {
                                ProductResponseDto product = productMap.get(itemDto.getProductId());
                                return OrderItem.builder()
                                        .sellerOrder(sellerOrder)
                                        .productId(product.getId())
                                        .productName(product.getName())
                                        .unitPrice(product.getPrice())
                                        .quantity(itemDto.getQuantity())
                                        .build();
                            })
                            .collect(Collectors.toList());

                    sellerOrder.setItems(orderItems);

                    BigDecimal subTotal = orderItems.stream()
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

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with id: {}", savedOrder.getId());

        return OrderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> findOrdersByClientId(UUID clientId, int page, int size) {
        log.info("Finding orders for client: {} - page: {}, size: {}", clientId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
        return orders.map(OrderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID id) {
        log.info("Finding order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format("Order with id = %s not found.", id)
                ));
        return OrderMapper.toDto(order);
    }

    @Transactional
    public void confirmOrder(PaymentConfirmedEvent event) {
        log.info("Confirming order with id: {}", event.orderId());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format("Order with id = %s not found.", event.orderId())
                ));

        if (order.getStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            log.warn("Order {} is not in PENDING_PAYMENT status. Current status: {}", order.getId(), order.getStatus());
            return;
        }

        order.setStatus(Order.OrderStatus.CONFIRMED);
        for (SellerOrder sellerOrder : order.getSellerOrders()) {
            sellerOrder.setStatus(SellerOrder.SellerOrderStatus.CONFIRMED);
        }
        orderRepository.save(order);

        log.info("Order {} confirmed successfully. Sending event to catalog for stock deduction.", order.getId());

        List<OrderConfirmedEvent.ProductStockItem> stockItems = order.getSellerOrders().stream()
                .flatMap(sellerOrder -> sellerOrder.getItems().stream())
                .map(item -> new OrderConfirmedEvent.ProductStockItem(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .toList();

        OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                order.getId(),
                order.getClientId(),
                stockItems
        );

        orderConfirmedProducer.send(confirmedEvent);
        log.info("Stock deduction event sent for order: {}", order.getId());
    }
}