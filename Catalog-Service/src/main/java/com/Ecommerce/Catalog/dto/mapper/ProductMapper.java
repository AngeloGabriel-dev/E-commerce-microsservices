package com.Ecommerce.Catalog.dto.mapper;

import com.Ecommerce.Catalog.dto.ProductCreateDto;
import com.Ecommerce.Catalog.dto.ProductResponseDto;
import com.Ecommerce.Catalog.entity.Product;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static Product toProduct(ProductCreateDto dto) {
        return new ModelMapper().map(dto, Product.class);
    }

    public static ProductResponseDto toDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .active(product.getActive())
                .category(product.getCategory())
                .sku(product.getSku())
                .attributes(product.getAttributes())
                .imageUrls(product.getImageUrls())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static List<ProductResponseDto> toListDto(List<Product> products) {
        return products.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }
}