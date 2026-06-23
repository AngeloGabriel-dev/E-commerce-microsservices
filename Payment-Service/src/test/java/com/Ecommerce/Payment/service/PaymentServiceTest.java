package com.Ecommerce.Payment.service;

import com.Ecommerce.Payment.dto.PaymentCreateDto;
import com.Ecommerce.Payment.dto.PaymentResponseDto;
import com.Ecommerce.Payment.dto.mapper.PaymentMapper;
import com.Ecommerce.Payment.entity.Payment;
import com.Ecommerce.Payment.exception.PaymentNotFoundException;
import com.Ecommerce.Payment.kafka.producer.PaymentConfirmedProducer;
import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs;
import com.Ecommerce.Payment.mercadopago.MercadoPagoGateway;
import com.Ecommerce.Payment.repository.PaymentRepository;
import com.Ecommerce.common.kafka.event.payment.PaymentConfirmedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentConfirmedProducer paymentConfirmedProducer;

    @Mock
    private MercadoPagoGateway mercadoPagoGateway;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<PaymentConfirmedEvent> eventCaptor;

    private UUID orderId;
    private UUID clientId;
    private UUID paymentId;
    private Payment payment;
    private PaymentCreateDto createDto;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        payment = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .clientId(clientId)
                .totalPrice(new BigDecimal("100.00"))
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod("credit_card")
                .mpPreferenceId("123456789")
                .mpInitPoint("https://mercadopago.com/checkout/123456789")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createDto = new PaymentCreateDto();
        createDto.setOrderId(orderId);
        createDto.setClientId(clientId);
        createDto.setTotalPrice(new BigDecimal("100.00"));
        createDto.setPaymentMethod("credit_card");
    }

    @Test
    @DisplayName("Should process payment successfully with Mercado Pago preference")
    void processPaymentSuccess() {
        MercadoPagoDTOs.PaymentPreferenceResponse preferenceResponse =
                MercadoPagoDTOs.PaymentPreferenceResponse.builder()
                        .id("MP-123456789")
                        .initPoint("https://mercadopago.com/checkout/init")
                        .externalReference(orderId.toString())
                        .status("created")
                        .build();

        when(mercadoPagoGateway.createPreference(any(MercadoPagoDTOs.PaymentPreferenceRequest.class)))
                .thenReturn(preferenceResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDto response = paymentService.processPayment(createDto);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getMpPreferenceId()).isEqualTo("MP-123456789");
        assertThat(response.getMpInitPoint()).isEqualTo("https://mercadopago.com/checkout/init");

        verify(mercadoPagoGateway).createPreference(any(MercadoPagoDTOs.PaymentPreferenceRequest.class));
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getMpPreferenceId()).isEqualTo("MP-123456789");
        assertThat(savedPayment.getMpInitPoint()).isEqualTo("https://mercadopago.com/checkout/init");
    }

    @Test
    @DisplayName("Should confirm payment and send Kafka event")
    void confirmPaymentSuccess() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDto response = paymentService.confirmPayment(paymentId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");

        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(Payment.PaymentStatus.CONFIRMED);

        verify(paymentConfirmedProducer).send(eventCaptor.capture());
        PaymentConfirmedEvent event = eventCaptor.getValue();
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.clientId()).isEqualTo(clientId);
        assertThat(event.totalPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should throw exception when confirming non-existent payment")
    void confirmPaymentNotFound() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment(paymentId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("not found");

        verify(paymentRepository, never()).save(any());
        verify(paymentConfirmedProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should throw exception when confirming already confirmed payment")
    void confirmPaymentAlreadyConfirmed() {
        payment.setStatus(Payment.PaymentStatus.CONFIRMED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirmPayment(paymentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in PENDING status");

        verify(paymentRepository, never()).save(any());
        verify(paymentConfirmedProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should process webhook notification for approved payment")
    void processWebhookApproved() {
        Long mpPaymentId = 12345L;

        MercadoPagoDTOs.MercadoPagoPaymentResponse mpResponse =
                MercadoPagoDTOs.MercadoPagoPaymentResponse.builder()
                        .id(mpPaymentId)
                        .status("approved")
                        .externalReference(orderId.toString())
                        .transactionAmount(new BigDecimal("100.00"))
                        .build();

        when(mercadoPagoGateway.getPayment(mpPaymentId)).thenReturn(mpResponse);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDto response = paymentService.processWebhookNotification(mpPaymentId, "payment");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");

        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getMpPaymentId()).isEqualTo(mpPaymentId);
        assertThat(savedPayment.getMpStatus()).isEqualTo("approved");
        assertThat(savedPayment.getStatus()).isEqualTo(Payment.PaymentStatus.CONFIRMED);

        verify(paymentConfirmedProducer).send(eventCaptor.capture());
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("Should process webhook notification for rejected payment")
    void processWebhookRejected() {
        Long mpPaymentId = 12346L;

        MercadoPagoDTOs.MercadoPagoPaymentResponse mpResponse =
                MercadoPagoDTOs.MercadoPagoPaymentResponse.builder()
                        .id(mpPaymentId)
                        .status("rejected")
                        .externalReference(orderId.toString())
                        .transactionAmount(new BigDecimal("100.00"))
                        .build();

        when(mercadoPagoGateway.getPayment(mpPaymentId)).thenReturn(mpResponse);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDto response = paymentService.processWebhookNotification(mpPaymentId, "payment");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("FAILED");

        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);

        verify(paymentConfirmedProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should process webhook notification for refunded payment")
    void processWebhookRefunded() {
        Long mpPaymentId = 12347L;

        MercadoPagoDTOs.MercadoPagoPaymentResponse mpResponse =
                MercadoPagoDTOs.MercadoPagoPaymentResponse.builder()
                        .id(mpPaymentId)
                        .status("refunded")
                        .externalReference(orderId.toString())
                        .transactionAmount(new BigDecimal("100.00"))
                        .build();

        when(mercadoPagoGateway.getPayment(mpPaymentId)).thenReturn(mpResponse);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponseDto response = paymentService.processWebhookNotification(mpPaymentId, "payment");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("REFUNDED");

        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);

        verify(paymentConfirmedProducer, never()).send(any());
    }

    @Test
    @DisplayName("Should find payments by client ID with pagination")
    void findPaymentsByClientIdSuccess() {
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment));
        when(paymentRepository.findByClientIdOrderByCreatedAtDesc(eq(clientId), any(PageRequest.class)))
                .thenReturn(paymentPage);

        Page<PaymentResponseDto> response = paymentService.findPaymentsByClientId(clientId, 0, 10);

        assertThat(response).isNotEmpty();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getOrderId()).isEqualTo(orderId);

        verify(paymentRepository).findByClientIdOrderByCreatedAtDesc(eq(clientId), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should get payment by ID")
    void getPaymentByIdSuccess() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentResponseDto response = paymentService.getPaymentById(paymentId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(paymentId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("Should throw exception when payment by ID not found")
    void getPaymentByIdNotFound() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(paymentId))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("Should get payment by order ID")
    void getPaymentByOrderIdSuccess() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        PaymentResponseDto response = paymentService.getPaymentByOrderId(orderId);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("Should throw exception when payment by order ID not found")
    void getPaymentByOrderIdNotFound() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(orderId))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}