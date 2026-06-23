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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentConfirmedProducer paymentConfirmedProducer;
    private final MercadoPagoGateway mercadoPagoGateway;

    @Transactional
    public PaymentResponseDto processPayment(PaymentCreateDto dto) {
        log.info("Processing payment for order: {}", dto.getOrderId());

        Payment payment = PaymentMapper.toPayment(dto);

        // Create Mercado Pago preference
        MercadoPagoDTOs.PaymentPreferenceRequest preferenceRequest = MercadoPagoDTOs.PaymentPreferenceRequest.builder()
                .externalReference(dto.getOrderId().toString())
                .title("Order " + dto.getOrderId())
                .description("Payment for order " + dto.getOrderId())
                .totalAmount(dto.getTotalPrice())
                .currencyId("BRL")
                .quantity(1)
                .build();

        MercadoPagoDTOs.PaymentPreferenceResponse preferenceResponse = mercadoPagoGateway.createPreference(preferenceRequest);

        payment.setMpPreferenceId(preferenceResponse.getId());
        payment.setMpInitPoint(preferenceResponse.getInitPoint());

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created successfully with id: {}, mp preference: {}", savedPayment.getId(), preferenceResponse.getId());

        return PaymentMapper.toDto(savedPayment);
    }

    @Transactional
    public PaymentResponseDto confirmPayment(UUID paymentId) {
        log.info("Confirming payment with id: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        String.format("Payment with id = %s not found.", paymentId)
                ));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            log.warn("Payment {} is not in PENDING status. Current status: {}", payment.getId(), payment.getStatus());
            throw new IllegalStateException(
                    String.format("Payment with id = %s is not in PENDING status.", paymentId)
            );
        }

        payment.setStatus(Payment.PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);
        log.info("Payment {} confirmed successfully.", payment.getId());

        PaymentConfirmedEvent event = new PaymentConfirmedEvent(
                payment.getOrderId(),
                payment.getClientId(),
                payment.getTotalPrice(),
                java.util.List.of()
        );

        paymentConfirmedProducer.send(event);
        log.info("Payment confirmed event sent for order: {}", payment.getOrderId());

        return PaymentMapper.toDto(payment);
    }

    @Transactional
    public PaymentResponseDto processWebhookNotification(Long mpPaymentId, String topic) {
        log.info("Processing webhook notification - paymentId: {}, topic: {}", mpPaymentId, topic);

        MercadoPagoDTOs.MercadoPagoPaymentResponse mpPayment = mercadoPagoGateway.getPayment(mpPaymentId);

        Payment payment = paymentRepository.findByOrderId(UUID.fromString(mpPayment.getExternalReference()))
                .orElseThrow(() -> new PaymentNotFoundException(
                        String.format("Payment for order with id = %s not found.", mpPayment.getExternalReference())
                ));

        payment.setMpPaymentId(mpPayment.getId());
        payment.setMpStatus(mpPayment.getStatus());

        switch (mpPayment.getStatus()) {
            case "approved" -> {
                payment.setStatus(Payment.PaymentStatus.CONFIRMED);
                PaymentConfirmedEvent event = new PaymentConfirmedEvent(
                        payment.getOrderId(),
                        payment.getClientId(),
                        payment.getTotalPrice(),
                        java.util.List.of()
                );
                paymentConfirmedProducer.send(event);
                log.info("Payment confirmed via webhook for order: {}", payment.getOrderId());
            }
            case "rejected", "cancelled" -> {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                log.warn("Payment failed for order: {}", payment.getOrderId());
            }
            case "refunded" -> {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                log.info("Payment refunded for order: {}", payment.getOrderId());
            }
            default -> log.info("Payment status {} received for order: {}", mpPayment.getStatus(), payment.getOrderId());
        }

        paymentRepository.save(payment);
        return PaymentMapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> findPaymentsByClientId(UUID clientId, int page, int size) {
        log.info("Finding payments for client: {} - page: {}, size: {}", clientId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Payment> payments = paymentRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
        return payments.map(PaymentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(UUID id) {
        log.info("Finding payment by id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(
                        String.format("Payment with id = %s not found.", id)
                ));
        return PaymentMapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(UUID orderId) {
        log.info("Finding payment by order id: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        String.format("Payment for order with id = %s not found.", orderId)
                ));
        return PaymentMapper.toDto(payment);
    }
}
