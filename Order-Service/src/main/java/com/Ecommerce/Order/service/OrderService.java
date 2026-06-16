package com.Ecommerce.Order.service;

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

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderConfirmedProducer orderConfirmedProducer;

    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {
        log.info("Creating order for client: {}", dto.getClientId());

        Order order = OrderMapper.toOrder(dto);
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
                stockItems
        );

        orderConfirmedProducer.send(confirmedEvent);
        log.info("Stock deduction event sent for order: {}", order.getId());
    }
}