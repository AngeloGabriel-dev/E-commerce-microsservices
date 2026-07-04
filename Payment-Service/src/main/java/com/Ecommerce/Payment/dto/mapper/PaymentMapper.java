package com.Ecommerce.Payment.dto.mapper;

import com.Ecommerce.Payment.client.OrderResponseDto;
import com.Ecommerce.Payment.dto.PaymentCreateDto;
import com.Ecommerce.Payment.dto.PaymentResponseDto;
import com.Ecommerce.Payment.entity.Payment;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaymentMapper {

    public static Payment toPayment(PaymentCreateDto dto, OrderResponseDto order) {
        return Payment.builder()
                .orderId(order.getId())
                .clientId(order.getClientId())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(dto.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .build();
    }

    public static PaymentResponseDto toDto(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .clientId(payment.getClientId())
                .totalPrice(payment.getTotalPrice())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .mpPaymentId(payment.getMpPaymentId())
                .mpPreferenceId(payment.getMpPreferenceId())
                .mpStatus(payment.getMpStatus())
                .mpInitPoint(payment.getMpInitPoint())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public static List<PaymentResponseDto> toListDto(List<Payment> payments) {
        return payments.stream()
                .map(PaymentMapper::toDto)
                .collect(Collectors.toList());
    }
}