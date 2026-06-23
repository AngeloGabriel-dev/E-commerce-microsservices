package com.Ecommerce.Payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentCreateDto {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}