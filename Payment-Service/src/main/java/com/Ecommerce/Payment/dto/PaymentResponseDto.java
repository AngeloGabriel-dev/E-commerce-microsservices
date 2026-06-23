package com.Ecommerce.Payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private UUID id;
    private UUID orderId;
    private UUID clientId;
    private BigDecimal totalPrice;
    private String status;
    private String paymentMethod;
    private Long mpPaymentId;
    private String mpPreferenceId;
    private String mpStatus;
    private String mpInitPoint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
