package com.Ecommerce.Order.service;

import com.Ecommerce.Order.client.CatalogServiceClient;
import com.Ecommerce.Order.client.ProductResponseDto;
import com.Ecommerce.Order.dto.OrderCreateDto;
import com.Ecommerce.Order.dto.OrderResponseDto;
import com.Ecommerce.Order.entity.Order;
import com.Ecommerce.Order.entity.OrderItem;
import com.Ecommerce.Order.entity.SellerOrder;
import com.Ecommerce.Order.exception.InvalidOrderException;
import com.Ecommerce.Order.exception.OrderNotFoundException;
import com.Ecommerce.Order.kafka.producer.OrderConfirmedProducer;
import com.Ecommerce.Order.repository.OrderRepository;
import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import com.Ecommerce.common.kafka.event.payment.PaymentConfirmedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderConfirmedProducer orderConfirmedProducer;

    @Mock
    private CatalogServiceClient catalogServiceClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderConfirmedProducer, catalogServiceClient);
    }

    private OrderCreateDto createValidOrderDto() {
        OrderCreateDto dto = new OrderCreateDto();

        OrderCreateDto.OrderItemDto itemDto1 = new OrderCreateDto.OrderItemDto();
        itemDto1.setProductId(UUID.randomUUID());
        itemDto1.setQuantity(2);

        OrderCreateDto.OrderItemDto itemDto2 = new OrderCreateDto.OrderItemDto();
        itemDto2.setProductId(UUID.randomUUID());
        itemDto2.setQuantity(1);

        dto.setItems(List.of(itemDto1, itemDto2));

        return dto;
    }

    private ProductResponseDto createProductResponse(UUID productId, UUID sellerId, String name, BigDecimal price) {
        return ProductResponseDto.builder()
                .id(productId)
                .sellerId(sellerId)
                .name(name)
                .price(price)
                .stock(100)
                .active(true)
                .build();
    }

    private Map<UUID, ProductResponseDto> createProductMap(OrderCreateDto dto, UUID sellerId) {
        return Map.of(
                dto.getItems().get(0).getProductId(),
                createProductResponse(dto.getItems().get(0).getProductId(), sellerId, "Product 1", new BigDecimal("10.00")),
                dto.getItems().get(1).getProductId(),
                createProductResponse(dto.getItems().get(1).getProductId(), sellerId, "Product 2", new BigDecimal("5.00"))
        );
    }

    @Nested
    @DisplayName("createOrder() - Create a new order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order with clientId from token, seller and product data from catalog")
        void createOrder_Success() {
            // Arrange
            UUID clientId = UUID.randomUUID();
            OrderCreateDto dto = createValidOrderDto();
            UUID sellerId = UUID.randomUUID();
            Map<UUID, ProductResponseDto> productMap = createProductMap(dto, sellerId);

            when(catalogServiceClient.fetchProducts(anyList())).thenReturn(productMap);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrderResponseDto result = orderService.createOrder(dto, clientId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getClientId()).isEqualTo(clientId);
            assertThat(result.getStatus()).isEqualTo("PENDING_PAYMENT");
            assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
            assertThat(result.getSellerOrders()).hasSize(1);
            assertThat(result.getSellerOrders().get(0).getSellerId()).isEqualTo(sellerId);
            assertThat(result.getSellerOrders().get(0).getItems()).hasSize(2);

            // Verify items have data from catalog, not from client
            OrderResponseDto.OrderItemResponseDto item1 = result.getSellerOrders().get(0).getItems().get(0);
            assertThat(item1.getProductId()).isEqualTo(dto.getItems().get(0).getProductId());
            assertThat(item1.getProductName()).isEqualTo("Product 1");
            assertThat(item1.getUnitPrice()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(item1.getQuantity()).isEqualTo(2);

            OrderResponseDto.OrderItemResponseDto item2 = result.getSellerOrders().get(0).getItems().get(1);
            assertThat(item2.getProductId()).isEqualTo(dto.getItems().get(1).getProductId());
            assertThat(item2.getProductName()).isEqualTo("Product 2");
            assertThat(item2.getUnitPrice()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(item2.getQuantity()).isEqualTo(1);

            verify(catalogServiceClient).fetchProducts(anyList());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should group items by sellerId from catalog data when products have different sellers")
        void createOrder_MultipleSellers() {
            // Arrange
            UUID clientId = UUID.randomUUID();
            OrderCreateDto dto = new OrderCreateDto();

            UUID productId1 = UUID.randomUUID();
            UUID productId2 = UUID.randomUUID();
            UUID sellerId1 = UUID.randomUUID();
            UUID sellerId2 = UUID.randomUUID();

            OrderCreateDto.OrderItemDto itemDto1 = new OrderCreateDto.OrderItemDto();
            itemDto1.setProductId(productId1);
            itemDto1.setQuantity(2);

            OrderCreateDto.OrderItemDto itemDto2 = new OrderCreateDto.OrderItemDto();
            itemDto2.setProductId(productId2);
            itemDto2.setQuantity(3);

            dto.setItems(List.of(itemDto1, itemDto2));

            Map<UUID, ProductResponseDto> productMap = Map.of(
                    productId1, createProductResponse(productId1, sellerId1, "Product A", new BigDecimal("10.00")),
                    productId2, createProductResponse(productId2, sellerId2, "Product B", new BigDecimal("20.00"))
            );

            when(catalogServiceClient.fetchProducts(anyList())).thenReturn(productMap);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            OrderResponseDto result = orderService.createOrder(dto, clientId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getClientId()).isEqualTo(clientId);
            assertThat(result.getSellerOrders()).hasSize(2);
            assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("80.00")); // 2*10 + 3*20

            verify(catalogServiceClient).fetchProducts(anyList());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw InvalidOrderException when a product does not exist in catalog")
        void createOrder_ProductNotFound() {
            // Arrange
            UUID clientId = UUID.randomUUID();
            OrderCreateDto dto = createValidOrderDto();

            when(catalogServiceClient.fetchProducts(anyList()))
                    .thenThrow(new InvalidOrderException("Product with id ... not found in catalog."));

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                    .isInstanceOf(InvalidOrderException.class)
                    .hasMessageContaining("Product");

            verify(catalogServiceClient).fetchProducts(anyList());
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("findOrdersByClientId() - Find orders by client with pagination")
    class FindOrdersByClientIdTests {

        @Test
        @DisplayName("Should return paginated orders for client")
        void findOrdersByClientId_Success() {
            // Arrange
            UUID clientId = UUID.randomUUID();
            int page = 0;
            int size = 10;

            Order order = createOrder();
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

            when(orderRepository.findByClientIdOrderByCreatedAtDesc(eq(clientId), any(Pageable.class))).thenReturn(orderPage);

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByClientId(clientId, page, size);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(order.getId());
            assertThat(result.getPageable().getPageNumber()).isEqualTo(page);
            assertThat(result.getPageable().getPageSize()).isEqualTo(size);

            verify(orderRepository).findByClientIdOrderByCreatedAtDesc(eq(clientId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when client has no orders")
        void findOrdersByClientId_Empty() {
            // Arrange
            UUID clientId = UUID.randomUUID();
            int page = 0;
            int size = 10;

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(orderRepository.findByClientIdOrderByCreatedAtDesc(eq(clientId), any(Pageable.class))).thenReturn(emptyPage);

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByClientId(clientId, page, size);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getOrderById() - Find order by ID")
    class GetOrderByIdTests {

        @Test
        @DisplayName("Should return order when found")
        void getOrderById_Found() {
            // Arrange
            UUID id = UUID.randomUUID();
            Order order = createOrder();
            order.setId(id);

            when(orderRepository.findById(eq(id))).thenReturn(java.util.Optional.of(order));

            // Act
            OrderResponseDto result = orderService.getOrderById(id);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getClientId()).isEqualTo(order.getClientId());
            assertThat(result.getStatus()).isEqualTo("PENDING_PAYMENT");

            verify(orderRepository).findById(eq(id));
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when order not found")
        void getOrderById_NotFound() {
            // Arrange
            UUID id = UUID.randomUUID();

            when(orderRepository.findById(eq(id))).thenReturn(java.util.Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.getOrderById(id))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(orderRepository).findById(eq(id));
        }
    }

    @Nested
    @DisplayName("confirmOrder() - Confirm order after payment")
    class ConfirmOrderTests {

        @Test
        @DisplayName("Should confirm order and send Kafka event when order is PENDING_PAYMENT")
        void confirmOrder_Success() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            Order order = createOrder();
            order.setId(orderId);
            order.setStatus(Order.OrderStatus.PENDING_PAYMENT);

            PaymentConfirmedEvent paymentEvent = new PaymentConfirmedEvent(
                    orderId,
                    order.getClientId(),
                    order.getTotalPrice(),
                    List.of(new PaymentConfirmedEvent.SellerOrderEvent(
                            order.getSellerOrders().get(0).getId(),
                            order.getSellerOrders().get(0).getSellerId(),
                            order.getSellerOrders().get(0).getSubTotal(),
                            order.getSellerOrders().get(0).getItems().stream()
                                    .map(item -> new PaymentConfirmedEvent.SellerOrderEvent.OrderItemEvent(
                                            item.getProductId(),
                                            item.getProductName(),
                                            item.getUnitPrice(),
                                            item.getQuantity()
                                    ))
                                    .toList()
                    ))
            );

            when(orderRepository.findById(eq(orderId))).thenReturn(java.util.Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            orderService.confirmOrder(paymentEvent);

            // Assert
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
            assertThat(order.getSellerOrders()).hasSize(1);
            assertThat(order.getSellerOrders().get(0).getStatus()).isEqualTo(SellerOrder.SellerOrderStatus.CONFIRMED);

            verify(orderRepository).findById(eq(orderId));
            verify(orderRepository).save(eq(order));
            verify(orderConfirmedProducer).send(any(OrderConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should not confirm order when order is not in PENDING_PAYMENT status")
        void confirmOrder_AlreadyConfirmed() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            Order order = createOrder();
            order.setStatus(Order.OrderStatus.CONFIRMED);

            PaymentConfirmedEvent paymentEvent = new PaymentConfirmedEvent(
                    orderId,
                    order.getClientId(),
                    order.getTotalPrice(),
                    List.of()
            );

            when(orderRepository.findById(eq(orderId))).thenReturn(java.util.Optional.of(order));

            // Act
            orderService.confirmOrder(paymentEvent);

            // Assert
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);

            verify(orderRepository).findById(eq(orderId));
            verify(orderRepository, never()).save(any(Order.class));
            verify(orderConfirmedProducer, never()).send(any(OrderConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when order does not exist")
        void confirmOrder_OrderNotFound() {
            // Arrange
            UUID orderId = UUID.randomUUID();

            PaymentConfirmedEvent paymentEvent = new PaymentConfirmedEvent(
                    orderId,
                    UUID.randomUUID(),
                    BigDecimal.ZERO,
                    List.of()
            );

            when(orderRepository.findById(eq(orderId))).thenReturn(java.util.Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.confirmOrder(paymentEvent))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining(orderId.toString());

            verify(orderRepository).findById(eq(orderId));
            verify(orderRepository, never()).save(any(Order.class));
            verify(orderConfirmedProducer, never()).send(any(OrderConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should send OrderConfirmedEvent with correct product list")
        void confirmOrder_CorrectKafkaEvent() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            Order order = createOrder();
            order.setId(orderId);
            order.setStatus(Order.OrderStatus.PENDING_PAYMENT);

            PaymentConfirmedEvent paymentEvent = new PaymentConfirmedEvent(
                    orderId,
                    order.getClientId(),
                    order.getTotalPrice(),
                    List.of(new PaymentConfirmedEvent.SellerOrderEvent(
                            order.getSellerOrders().get(0).getId(),
                            order.getSellerOrders().get(0).getSellerId(),
                            order.getSellerOrders().get(0).getSubTotal(),
                            order.getSellerOrders().get(0).getItems().stream()
                                    .map(item -> new PaymentConfirmedEvent.SellerOrderEvent.OrderItemEvent(
                                            item.getProductId(),
                                            item.getProductName(),
                                            item.getUnitPrice(),
                                            item.getQuantity()
                                    ))
                                    .toList()
                    ))
            );

            when(orderRepository.findById(eq(orderId))).thenReturn(java.util.Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<OrderConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);

            // Act
            orderService.confirmOrder(paymentEvent);

            // Assert
            verify(orderConfirmedProducer).send(eventCaptor.capture());
            OrderConfirmedEvent capturedEvent = eventCaptor.getValue();

            assertThat(capturedEvent).isNotNull();
            assertThat(capturedEvent.orderId()).isEqualTo(orderId);
            assertThat(capturedEvent.products()).hasSize(2);
            assertThat(capturedEvent.products().get(0).productId()).isEqualTo(order.getSellerOrders().get(0).getItems().get(0).getProductId());
            assertThat(capturedEvent.products().get(0).quantity()).isEqualTo(2);
            assertThat(capturedEvent.products().get(1).productId()).isEqualTo(order.getSellerOrders().get(0).getItems().get(1).getProductId());
            assertThat(capturedEvent.products().get(1).quantity()).isEqualTo(1);
        }
    }

    // Helper to create an Order entity (for tests that need persisted-like data)
    private Order createOrder() {
        UUID clientId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        OrderCreateDto dto = createValidOrderDto();
        Map<UUID, ProductResponseDto> productMap = createProductMap(dto, sellerId);
        return buildOrderFromProducts(dto, clientId, productMap);
    }

    private Order buildOrderFromProducts(OrderCreateDto dto, UUID clientId, Map<UUID, ProductResponseDto> productMap) {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .totalPrice(BigDecimal.ZERO)
                .build();

        UUID firstSellerId = productMap.values().iterator().next().getSellerId();
        SellerOrder sellerOrder = SellerOrder.builder()
                .id(UUID.randomUUID())
                .order(order)
                .sellerId(firstSellerId)
                .status(SellerOrder.SellerOrderStatus.PENDING_PAYMENT)
                .subTotal(BigDecimal.ZERO)
                .build();

        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> {
                    ProductResponseDto product = productMap.get(itemDto.getProductId());
                    return OrderItem.builder()
                            .id(UUID.randomUUID())
                            .sellerOrder(sellerOrder)
                            .productId(product.getId())
                            .productName(product.getName())
                            .unitPrice(product.getPrice())
                            .quantity(itemDto.getQuantity())
                            .build();
                })
                .toList();

        sellerOrder.setItems(items);
        BigDecimal subTotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sellerOrder.setSubTotal(subTotal);
        order.setSellerOrders(List.of(sellerOrder));

        BigDecimal totalPrice = order.getSellerOrders().stream()
                .map(SellerOrder::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);

        return order;
    }
}