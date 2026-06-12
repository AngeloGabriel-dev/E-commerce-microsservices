package com.Ecommerce.Catalog.web.controller;

import com.Ecommerce.Catalog.dto.ProductCreateDto;
import com.Ecommerce.Catalog.dto.ProductResponseDto;
import com.Ecommerce.Catalog.service.CatalogService;
import com.Ecommerce.Catalog.web.exceptions.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class CatalogController {

    private final CatalogService catalogService;

    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Create a new product", description = "Resource to create a new product. Only users with SELLER role can access.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Product created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = "Product SKU already registered",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid input data",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied. Only SELLER role can create products.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody @Valid ProductCreateDto dto,
                                                             Authentication authentication) {
        log.info("REST request to create product: {}", dto.getSku());
        UUID sellerId = UUID.fromString(authentication.getCredentials().toString());
        ProductResponseDto response = catalogService.createProduct(dto, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List all active products with pagination", description = "Resource to list all active products with pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
            })
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to list products - page: {}, size: {}", page, size);
        Page<ProductResponseDto> products = catalogService.findAllActiveProducts(page, size);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get recommended products", description = "Resource to get the latest recommended products",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recommended products retrieved successfully")
            })
    @GetMapping("/recommended")
    public ResponseEntity<List<ProductResponseDto>> getRecommendedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        log.info("REST request to get {} recommended products", limit);
        List<ProductResponseDto> products = catalogService.getRecommendedProducts(limit);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Search products", description = "Resource to search products by query",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
            })
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to search products - query: {}, page: {}, size: {}", q, page, size);
        Page<ProductResponseDto> products = catalogService.searchProducts(q, page, size);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get product by ID", description = "Resource to get a specific product by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Product not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable UUID id) {
        log.info("REST request to get product by id: {}", id);
        ProductResponseDto product = catalogService.getProductById(id);
        return ResponseEntity.ok(product);
    }
}