package com.Ecommerce.Catalog.service;

import com.Ecommerce.Catalog.dto.ProductCreateDto;
import com.Ecommerce.Catalog.dto.ProductResponseDto;
import com.Ecommerce.Catalog.entity.Product;
import com.Ecommerce.Catalog.exception.ProductNotFoundException;
import com.Ecommerce.Catalog.exception.SkuUniqueViolationException;
import com.Ecommerce.Catalog.repository.ProductRepository;
import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private CatalogService catalogService;

    private UUID sellerId;
    private UUID productId;
    private Product product;
    private ProductCreateDto createDto;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(productRepository);

        sellerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setSellerId(sellerId);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(10);
        product.setActive(true);
        product.setCategory("Electronics");
        product.setSku("SKU-001");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        createDto = new ProductCreateDto();
        createDto.setName("Test Product");
        createDto.setDescription("Test Description");
        createDto.setPrice(new BigDecimal("99.99"));
        createDto.setStock(10);
        createDto.setCategory("Electronics");
        createDto.setSku("SKU-001");
    }

    @Nested
    @DisplayName("createProduct() - Create product")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_Success() {
            when(productRepository.existsBySku(createDto.getSku())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(product);

            ProductResponseDto result = catalogService.createProduct(createDto, sellerId);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getSku()).isEqualTo("SKU-001");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(result.getStock()).isEqualTo(10);

            verify(productRepository).save(productCaptor.capture());
            Product captured = productCaptor.getValue();
            assertThat(captured.getSellerId()).isEqualTo(sellerId);
            assertThat(captured.getActive()).isTrue();
            assertThat(captured.getName()).isEqualTo(createDto.getName());
            assertThat(captured.getSku()).isEqualTo(createDto.getSku());
        }

        @Test
        @DisplayName("Should throw SkuUniqueViolationException when SKU already exists")
        void createProduct_DuplicateSku() {
            when(productRepository.existsBySku(createDto.getSku())).thenReturn(true);

            assertThatThrownBy(() -> catalogService.createProduct(createDto, sellerId))
                    .isInstanceOf(SkuUniqueViolationException.class)
                    .hasMessageContaining(createDto.getSku());

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw SkuUniqueViolationException on DataIntegrityViolation")
        void createProduct_DataIntegrityViolation() {
            when(productRepository.existsBySku(createDto.getSku())).thenReturn(false);
            when(productRepository.save(any(Product.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique violation"));

            assertThatThrownBy(() -> catalogService.createProduct(createDto, sellerId))
                    .isInstanceOf(SkuUniqueViolationException.class)
                    .hasMessageContaining(createDto.getSku());
        }
    }

    @Nested
    @DisplayName("findAllActiveProducts() - List active products")
    class FindAllActiveProductsTests {

        @Test
        @DisplayName("Should return paginated active products")
        void findAllActiveProducts_Success() {
            Page<Product> productPage = new PageImpl<>(List.of(product));
            when(productRepository.findByActiveTrue(any(PageRequest.class))).thenReturn(productPage);

            Page<ProductResponseDto> result = catalogService.findAllActiveProducts(0, 10);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getName()).isEqualTo("Test Product");

            verify(productRepository).findByActiveTrue(any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("searchProducts() - Search products")
    class SearchProductsTests {

        @Test
        @DisplayName("Should return paginated search results")
        void searchProducts_Success() {
            Page<Product> productPage = new PageImpl<>(List.of(product));
            when(productRepository.searchByQuery(anyString(), any(PageRequest.class))).thenReturn(productPage);

            Page<ProductResponseDto> result = catalogService.searchProducts("test", 0, 10);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(1);

            verify(productRepository).searchByQuery(eq("test"), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("getRecommendedProducts() - Get recommended products")
    class GetRecommendedProductsTests {

        @Test
        @DisplayName("Should return top products ordered by creation date")
        void getRecommendedProducts_Success() {
            when(productRepository.findTopByActiveTrueOrderByCreatedAtDesc(any(PageRequest.class)))
                    .thenReturn(List.of(product));

            List<ProductResponseDto> result = catalogService.getRecommendedProducts(5);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getName()).isEqualTo("Test Product");
        }
    }

    @Nested
    @DisplayName("getProductById() - Find product by ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when ID exists")
        void getProductById_Found() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            ProductResponseDto result = catalogService.getProductById(productId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(productId);
            assertThat(result.getName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when ID does not exist")
        void getProductById_NotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> catalogService.getProductById(productId))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining(productId.toString());
        }
    }

    @Nested
    @DisplayName("deductStock() - Deduct stock from products")
    class DeductStockTests {

        @Test
        @DisplayName("Should deduct stock successfully for all products")
        void deductStock_Success() {
            OrderConfirmedEvent.ProductStockItem stockItem =
                    new OrderConfirmedEvent.ProductStockItem(productId, 3);
            OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID(), UUID.randomUUID(), List.of(stockItem));

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            catalogService.deductStock(event);

            verify(productRepository).save(productCaptor.capture());
            Product saved = productCaptor.getValue();
            assertThat(saved.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product does not exist")
        void deductStock_ProductNotFound() {
            OrderConfirmedEvent.ProductStockItem stockItem =
                    new OrderConfirmedEvent.ProductStockItem(productId, 3);
            OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID(), UUID.randomUUID(), List.of(stockItem));

            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> catalogService.deductStock(event))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when insufficient stock")
        void deductStock_InsufficientStock() {
            OrderConfirmedEvent.ProductStockItem stockItem =
                    new OrderConfirmedEvent.ProductStockItem(productId, 20);
            OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID(), UUID.randomUUID(), List.of(stockItem));

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> catalogService.deductStock(event))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient stock");
        }
    }
}
