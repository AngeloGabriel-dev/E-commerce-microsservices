package com.Ecommerce.Payment.client;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private UUID id;
    private UUID clientId;
    private String status;
    private BigDecimal totalPrice;
}