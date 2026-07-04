package com.Ecommerce.Order.client;

import com.Ecommerce.Order.exception.InvalidOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class CatalogServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.catalog-service.url}")
    private String catalogServiceUrl;

    /**
     * Fetches all products from the catalog by their IDs.
     * Returns a map of productId -> ProductResponseDto.
     * Throws InvalidOrderException if any product is not found.
     */
    public Map<UUID, ProductResponseDto> fetchProducts(List<UUID> productIds) {
        Set<UUID> uniqueProductIds = productIds.stream()
                .distinct()
                .collect(Collectors.toSet());

        log.info("Fetching {} unique products from catalog", uniqueProductIds.size());

        Map<UUID, ProductResponseDto> productMap = uniqueProductIds.stream()
                .map(this::fetchProductById)
                .collect(Collectors.toMap(ProductResponseDto::getId, Function.identity()));

        log.info("All {} products fetched successfully from catalog", productMap.size());
        return productMap;
    }

    private ProductResponseDto fetchProductById(UUID productId) {
        try {
            String url = catalogServiceUrl + "/api/v1/products/" + productId;
            ProductResponseDto product = restTemplate.getForObject(url, ProductResponseDto.class);
            log.debug("Product {} fetched successfully from catalog", productId);
            return product;
        } catch (Exception ex) {
            log.error("Product with id {} not found in catalog", productId);
            throw new InvalidOrderException(
                    String.format("Product with id %s not found in catalog.", productId)
            );
        }
    }
}