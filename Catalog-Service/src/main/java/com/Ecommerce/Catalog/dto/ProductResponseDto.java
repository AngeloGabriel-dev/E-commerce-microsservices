package com.Ecommerce.Catalog.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
    private String category;
    private String sku;
    private Map<String, Object> attributes;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}