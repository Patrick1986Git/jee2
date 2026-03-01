package com.company.shop.module.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.shop.module.category.entity.Category;
import com.company.shop.module.category.repository.CategoryRepository;
import com.company.shop.module.product.dto.ProductCreateDTO;
import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.exception.ProductCategoryNotFoundException;
import com.company.shop.module.product.exception.ProductSkuAlreadyExistsException;
import com.company.shop.module.product.mapper.ProductMapper;
import com.company.shop.module.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(productRepository, categoryRepository, productMapper);
    }

    @Test
    void create_shouldThrowWhenSkuAlreadyExists() {
        ProductCreateDTO dto = dto("Test Product", "SKU-123", UUID.randomUUID());
        when(productRepository.existsBySku(dto.getSku())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ProductSkuAlreadyExistsException.class)
                .hasMessageContaining(dto.getSku());

        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void create_shouldThrowWhenCategoryNotFound() {
        ProductCreateDTO dto = dto("Test Product", "SKU-123", UUID.randomUUID());
        when(productRepository.existsBySku(dto.getSku())).thenReturn(false);
        when(categoryRepository.findById(dto.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ProductCategoryNotFoundException.class)
                .hasMessageContaining(dto.getCategoryId().toString());

        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void create_shouldGenerateDeterministicSlugSuffixWhenBaseSlugExists() {
        UUID categoryId = UUID.randomUUID();
        ProductCreateDTO dto = dto("Phone Case", "SKU-123", categoryId);
        Category category = new Category("Accessories", "accessories", "desc");

        when(productRepository.existsBySku(dto.getSku())).thenReturn(false);
        when(categoryRepository.findById(dto.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.existsBySlug("phone-case")).thenReturn(true);
        when(productRepository.existsBySlug("phone-case-2")).thenReturn(false);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(any(Product.class))).thenReturn(stubResponse());

        service.create(dto);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).saveAndFlush(productCaptor.capture());
        assertThat(productCaptor.getValue().getSlug()).isEqualTo("phone-case-2");
    }

    @Test
    void update_shouldUpdateSkuInAggregate() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        ProductCreateDTO dto = dto("Test Product", "NEW-SKU", categoryId);

        Category category = new Category("Accessories", "accessories", "desc");
        Product existing = new Product("Old", "old", "OLD-SKU", "desc", BigDecimal.TEN, 2, category);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuAndIdNot(dto.getSku(), productId)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.existsBySlugAndIdNot("test-product", productId)).thenReturn(false);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toDto(any(Product.class))).thenReturn(stubResponse());

        service.update(productId, dto);

        assertThat(existing.getSku()).isEqualTo("NEW-SKU");
    }

    private ProductCreateDTO dto(String name, String sku, UUID categoryId) {
        return new ProductCreateDTO(name, sku, "Description", BigDecimal.valueOf(19.99), 10, categoryId,
                List.of("https://img.example/1.png"));
    }

    private ProductResponseDTO stubResponse() {
        return new ProductResponseDTO(UUID.randomUUID(), "name", "slug", "sku", "desc", BigDecimal.ONE,
                1, UUID.randomUUID(), "cat", 0.0, 0, List.of());
    }
}
