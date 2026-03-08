package com.company.shop.module.product.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.dto.ProductSearchCriteria;
import com.company.shop.module.product.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductResponseDTO> getProducts(@PageableDefault(size = 12) Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping("/category/{categoryId}")
    public Page<ProductResponseDTO> getProductsByCategory(
            @PathVariable UUID categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        return productService.findAllByCategory(categoryId, pageable);
    }

    @GetMapping("/slug/{slug}")
    public ProductResponseDTO getProductBySlug(@PathVariable String slug) {
        return productService.findBySlug(slug);
    }

    @GetMapping("/search")
    public Page<ProductResponseDTO> searchProducts(
            @Valid @ModelAttribute ProductSearchCriteria criteria,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.searchProducts(criteria, pageable);
    }
}
