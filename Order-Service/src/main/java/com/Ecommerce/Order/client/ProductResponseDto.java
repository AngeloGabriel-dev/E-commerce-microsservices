package com.Ecommerce.Order.client;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private UUID id;
    private UUID sellerId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
}
