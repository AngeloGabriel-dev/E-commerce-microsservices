package com.Ecommerce.Payment.service;

import com.Ecommerce.Payment.dto.PaymentCreateDto;
import com.Ecommerce.Payment.dto.PaymentResponseDto;
import com.Ecommerce.Payment.entity.Payment;
import com.Ecommerce.Payment.exception.PaymentNotFoundException;
import com.Ecommerce.Payment.kafka.producer.PaymentConfirmedProducer;
import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs;
import com.Ecommerce.Payment.mercadopago.MercadoPagoGateway;
import com.Ecommerce.Payment.repository.PaymentRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentConfirmedProducer paymentConfirmedProducer;

    @Mock
    private MercadoPagoGateway mercadoPagoGateway;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, paymentConfirmedProducer, mercadoPagoGateway);
    }

    private PaymentCreateDto createValidPaymentDto() {
        PaymentCreateDto dto = new PaymentCreateDto();
        dto.setOrderId(UUID.randomUUID());
        dto.setClientId(UUID.randomUUID());
        dto.setTotalPrice(new BigDecimal("100.00"));
        dto.setPaymentMethod("credit_card");
        return dto;
    }

    private Payment createPayment(PaymentCreateDto dto) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .orderId(dto.getOrderId())
                .clientId(dto.getClientId())
                .totalPrice(dto.getTotalPrice())
                .paymentMethod(dto.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .build();
    }
    @Nested
    @DisplayName("processPayment() - Create payment with Mercado Pago")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should create payment preference and return response DTO")
        void processPayment_Success() {
            // Arrange
            PaymentCreateDto dto = createValidPaymentDto();
            Payment payment = createPayment(dto);

            MercadoPagoDTOs.PaymentPreferenceResponse preferenceResponse = MercadoPagoDTOs.PaymentPreferenceResponse.builder()
                    .id("pref_123")
                    .initPoint("https://mercadopago.com/checkout?pref_id=pref_123")
                    .build();

            when(mercadoPagoGateway.createPreference(any(MercadoPagoDTOs.PaymentPreferenceRequest.class))).thenReturn(preferenceResponse);
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // Act
            PaymentResponseDto result = paymentService.processPayment(dto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(dto.getOrderId());
            assertThat(result.getStatus()).isEqualTo("PENDING");
            assertThat(result.getMpPreferenceId()).isEqualTo("pref_123");
            assertThat(result.getMpInitPoint()).isEqualTo("https://mercadopago.com/checkout?pref_id=pref_123");

            verify(mercadoPagoGateway).createPreference(any(MercadoPagoDTOs.PaymentPreferenceRequest.class));
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("confirmPayment() - Confirm payment and send event")
    class ConfirmPaymentTests {

        @Test
        @DisplayName("Should confirm pending payment and send Kafka event")
        void confirmPayment_Success() {
            // Arrange
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(createValidPaymentDto());
            payment.setId(paymentId);

            when(paymentRepository.findById(eq(paymentId))).thenReturn(java.util.Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PaymentResponseDto result = paymentService.confirmPayment(paymentId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("CONFIRMED");

            verify(paymentRepository).findById(eq(paymentId));
            verify(paymentRepository).save(eq(payment));
            verify(paymentConfirmedProducer).send(any(PaymentConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when payment is not PENDING")
        void confirmPayment_NotPending() {
            // Arrange
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(createValidPaymentDto());
            payment.setId(paymentId);
            payment.setStatus(Payment.PaymentStatus.CONFIRMED);

            when(paymentRepository.findById(eq(paymentId))).thenReturn(java.util.Optional.of(payment));

            // Act & Assert
            assertThatThrownBy(() -> paymentService.confirmPayment(paymentId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(paymentId.toString());

            verify(paymentRepository).findById(eq(paymentId));
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(paymentConfirmedProducer, never()).send(any(PaymentConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when payment does not exist")
        void confirmPayment_NotFound() {
            // Arrange
            UUID paymentId = UUID.randomUUID();

            when(paymentRepository.findById(eq(paymentId))).thenReturn(java.util.Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> paymentService.confirmPayment(paymentId))
                    .isInstanceOf(PaymentNotFoundException.class)
                    .hasMessageContaining(paymentId.toString());

            verify(paymentRepository).findById(eq(paymentId));
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(paymentConfirmedProducer, never()).send(any(PaymentConfirmedEvent.class));
        }
    }

    @Nested
    @DisplayName("processWebhookNotification() - Process Mercado Pago webhook")
    class ProcessWebhookNotificationTests {

        @Test
        @DisplayName("Should confirm payment and send event when status is approved")
        void processWebhookNotification_Approved() {
            // Arrange
            Long mpPaymentId = 12345L;
            String topic = "payment";
            Payment payment = createPayment(createValidPaymentDto());
            payment.setStatus(Payment.PaymentStatus.PENDING);

            MercadoPagoDTOs.MercadoPagoPaymentResponse mpPayment = MercadoPagoDTOs.MercadoPagoPaymentResponse.builder()
                    .id(mpPaymentId)
                    .status("approved")
                    .externalReference(payment.getOrderId().toString())
                    .build();

            when(mercadoPagoGateway.getPayment(eq(mpPaymentId))).thenReturn(mpPayment);
            when(paymentRepository.findByOrderId(eq(payment.getOrderId()))).thenReturn(java.util.Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PaymentResponseDto result = paymentService.processWebhookNotification(mpPaymentId, topic);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("CONFIRMED");
            assertThat(result.getMpPaymentId()).isEqualTo(mpPaymentId);
            assertThat(result.getMpStatus()).isEqualTo("approved");

            verify(mercadoPagoGateway).getPayment(eq(mpPaymentId));
            verify(paymentRepository).findByOrderId(eq(payment.getOrderId()));
            verify(paymentRepository).save(eq(payment));
            verify(paymentConfirmedProducer).send(any(PaymentConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should mark payment as FAILED when status is rejected")
        void processWebhookNotification_Rejected() {
            // Arrange
            Long mpPaymentId = 12346L;
            String topic = "payment";
            Payment payment = createPayment(createValidPaymentDto());
            payment.setStatus(Payment.PaymentStatus.PENDING);

            MercadoPagoDTOs.MercadoPagoPaymentResponse mpPayment = MercadoPagoDTOs.MercadoPagoPaymentResponse.builder()
                    .id(mpPaymentId)
                    .status("rejected")
                    .externalReference(payment.getOrderId().toString())
                    .build();

            when(mercadoPagoGateway.getPayment(eq(mpPaymentId))).thenReturn(mpPayment);
            when(paymentRepository.findByOrderId(eq(payment.getOrderId()))).thenReturn(java.util.Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PaymentResponseDto result = paymentService.processWebhookNotification(mpPaymentId, topic);

            // Assert
            assertThat(result.getStatus()).isEqualTo("FAILED");
            verify(paymentRepository).save(eq(payment));
            verify(paymentConfirmedProducer, never()).send(any(PaymentConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should mark payment as REFUNDED when status is refunded")
        void processWebhookNotification_Refunded() {
            // Arrange
            Long mpPaymentId = 12347L;
            String topic = "payment";
            Payment payment = createPayment(createValidPaymentDto());

            MercadoPagoDTOs.MercadoPagoPaymentResponse mpPayment = MercadoPagoDTOs.MercadoPagoPaymentResponse.builder()
                    .id(mpPaymentId)
                    .status("refunded")
                    .externalReference(payment.getOrderId().toString())
                    .build();

            when(mercadoPagoGateway.getPayment(eq(mpPaymentId))).thenReturn(mpPayment);
            when(paymentRepository.findByOrderId(eq(payment.getOrderId()))).thenReturn(java.util.Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PaymentResponseDto result = paymentService.processWebhookNotification(mpPaymentId, topic);

            // Assert
            assertThat(result.getStatus()).isEqualTo("REFUNDED");
            verify(paymentRepository).save(eq(payment));
            verify(paymentConfirmedProducer, never()).send(any(PaymentConfirmedEvent.class));
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when order not found")
        void processWebhookNotification_OrderNotFound() {
            // Arrange
            Long mpPaymentId = 12348L;
            String topic = "payment";

            MercadoPagoDTOs.MercadoPagoPaymentResponse mpPayment = MercadoPagoDTOs.MercadoPagoPaymentResponse.builder()
                    .id(mpPaymentId)
                    .status("approved")
                    .externalReference(UUID.randomUUID().toString())
                    .build();

            when(mercadoPagoGateway.getPayment(eq(mpPaymentId))).thenReturn(mpPayment);
            when(paymentRepository.findByOrderId(any(UUID.class))).thenReturn(java.util.Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> paymentService.processWebhookNotification(mpPaymentId, topic))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(paymentRepository, never()).save(any(Payment.class));
            verify(paymentConfirmedProducer, never()).send(any(PaymentConfirmedEvent.class));
        }
    }

    @Nested
    @DisplayName("findPaymentsByClientId() - Find payments by client with pagination")
    class FindPaymentsByClientIdTests {

        @Test
        @DisplayName("Should return paginated payments for client")
        void findPaymentsByClientId_Success() {
            // Arrange
            UUID clientId = UUID.randomUUID();
            int page = 0;
            int size = 10;

            Payment payment = createPayment(createValidPaymentDto());
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);

            when(paymentRepository.findByClientIdOrderByCreatedAtDesc(eq(clientId), any(Pageable.class))).thenReturn(paymentPage);

            // Act
            Page<PaymentResponseDto> result = paymentService.findPaymentsByClientId(clientId, page, size);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(payment.getId());

            verify(paymentRepository).findByClientIdOrderByCreatedAtDesc(eq(clientId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when client has no payments")
        void findPaymentsByClientId_Empty() {
            // Arrange
            UUID clientId = UUID.randomUUID();
            int page = 0;
            int size = 10;

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(paymentRepository.findByClientIdOrderByCreatedAtDesc(eq(clientId), any(Pageable.class))).thenReturn(emptyPage);

            // Act
            Page<PaymentResponseDto> result = paymentService.findPaymentsByClientId(clientId, page, size);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getPaymentById() - Find payment by ID")
    class GetPaymentByIdTests {

        @Test
        @DisplayName("Should return payment when found")
        void getPaymentById_Found() {
            // Arrange
            UUID id = UUID.randomUUID();
            Payment payment = createPayment(createValidPaymentDto());
            payment.setId(id);

            when(paymentRepository.findById(eq(id))).thenReturn(java.util.Optional.of(payment));

            // Act
            PaymentResponseDto result = paymentService.getPaymentById(id);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getOrderId()).isEqualTo(payment.getOrderId());

            verify(paymentRepository).findById(eq(id));
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when payment not found")
        void getPaymentById_NotFound() {
            // Arrange
            UUID id = UUID.randomUUID();

            when(paymentRepository.findById(eq(id))).thenReturn(java.util.Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> paymentService.getPaymentById(id))
                    .isInstanceOf(PaymentNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(paymentRepository).findById(eq(id));
        }
    }

    @Nested
    @DisplayName("getPaymentByOrderId() - Find payment by order ID")
    class GetPaymentByOrderIdTests {

        @Test
        @DisplayName("Should return payment when found")
        void getPaymentByOrderId_Found() {
            // Arrange
            UUID orderId = UUID.randomUUID();
            Payment payment = createPayment(createValidPaymentDto());

            when(paymentRepository.findByOrderId(eq(orderId))).thenReturn(java.util.Optional.of(payment));

            // Act
            PaymentResponseDto result = paymentService.getPaymentByOrderId(orderId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);

            verify(paymentRepository).findByOrderId(eq(orderId));
        }

        @Test
        @DisplayName("Should throw PaymentNotFoundException when payment not found")
        void getPaymentByOrderId_NotFound() {
            // Arrange
            UUID orderId = UUID.randomUUID();

            when(paymentRepository.findByOrderId(eq(orderId))).thenReturn(java.util.Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> paymentService.getPaymentByOrderId(orderId))
                    .isInstanceOf(PaymentNotFoundException.class)
                    .hasMessageContaining(orderId.toString());

            verify(paymentRepository).findByOrderId(eq(orderId));
        }
    }
}
