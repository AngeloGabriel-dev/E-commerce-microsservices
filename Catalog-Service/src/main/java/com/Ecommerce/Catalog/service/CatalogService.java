package com.Ecommerce.Catalog.service;

import com.Ecommerce.Catalog.dto.ProductCreateDto;
import com.Ecommerce.Catalog.dto.ProductResponseDto;
import com.Ecommerce.Catalog.dto.mapper.ProductMapper;
import com.Ecommerce.Catalog.entity.Product;
import com.Ecommerce.Catalog.exception.ProductNotFoundException;
import com.Ecommerce.Catalog.exception.SkuUniqueViolationException;
import com.Ecommerce.Catalog.repository.ProductRepository;
import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CatalogService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductCreateDto dto, UUID sellerId) {
        log.info("Creating product with SKU: {} for seller: {}", dto.getSku(), sellerId);

        if (productRepository.existsBySku(dto.getSku())) {
            throw new SkuUniqueViolationException(
                    String.format("Product with SKU {%s} already exists.", dto.getSku())
            );
        }

        try {
            Product product = ProductMapper.toProduct(dto);
            product.setSellerId(sellerId);
            product.setActive(true);
            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully with id: {} for seller: {}", savedProduct.getId(), sellerId);
            return ProductMapper.toDto(savedProduct);
        } catch (DataIntegrityViolationException ex) {
            throw new SkuUniqueViolationException(
                    String.format("Product with SKU {%s} already exists.", dto.getSku())
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllActiveProducts(int page, int size) {
        log.info("Finding all active products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(ProductMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String query, int page, int size) {
        log.info("Searching products with query: {} - page: {}, size: {}", query, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.searchByQuery(query, pageable);
        return products.map(ProductMapper::toDto);
    }

    @Transactional(readOnly = true)
    public java.util.List<ProductResponseDto> getRecommendedProducts(int limit) {
        log.info("Getting {} recommended products", limit);
        Pageable pageable = PageRequest.of(0, limit);
        java.util.List<Product> products = productRepository.findTopByActiveTrueOrderByCreatedAtDesc(pageable);
        return ProductMapper.toListDto(products);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID id) {
        log.info("Finding product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product with id = %s not found.", id)
                ));
        return ProductMapper.toDto(product);
    }

    @Transactional
    public void deductStock(OrderConfirmedEvent event) {
        log.info("Deducting stock for order: {}", event.orderId());

        for (OrderConfirmedEvent.ProductStockItem item : event.products()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            String.format("Product with id = %s not found.", item.productId())
                    ));

            if (product.getStock() < item.quantity()) {
                log.error("Insufficient stock for product {}. Available: {}, requested: {}",
                        product.getId(), product.getStock(), item.quantity());
                throw new IllegalArgumentException(
                        String.format("Insufficient stock for product %s. Available: %d, requested: %d",
                                product.getName(), product.getStock(), item.quantity())
                );
            }

            product.setStock(product.getStock() - item.quantity());
            productRepository.save(product);
            log.info("Stock deducted for product {}: {} remaining", product.getId(), product.getStock());
        }

        log.info("Stock deduction completed for order: {}", event.orderId());
    }
}
