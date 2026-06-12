package com.Ecommerce.Catalog.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductCreateDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must have between 2 and 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @DecimalMax(value = "999999999.99", message = "Price exceeds maximum allowed value")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 50, message = "SKU must have between 3 and 50 characters")
    private String sku;

    private Map<String, Object> attributes;

    private List<String> imageUrls;
}