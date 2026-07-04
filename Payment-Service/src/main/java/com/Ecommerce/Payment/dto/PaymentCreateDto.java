package com.Ecommerce.Payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentCreateDto {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}