package com.company.shop.module.product.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.dto.ProductSearchCriteria;
import com.company.shop.module.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Publiczne endpointy produktów i wyszukiwania.")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lista produktów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista produktów pobrana poprawnie.")
    })
    public Page<ProductResponseDTO> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        return productService.findAll(pageable);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lista produktów w kategorii")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkty kategorii pobrane poprawnie."),
            @ApiResponse(responseCode = "404", description = "Kategoria nie została znaleziona.")
    })
    public Page<ProductResponseDTO> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        return productService.findAllByCategory(categoryId, pageable);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Szczegóły produktu po slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt znaleziony."),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony.")
    })
    public ProductResponseDTO getProductBySlug(@PathVariable String slug) {
        return productService.findBySlug(slug);
    }

    @GetMapping("/search")
    @Operation(summary = "Wyszukiwanie produktów")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wyniki wyszukiwania pobrane poprawnie.")
    })
    public Page<ProductResponseDTO> searchProducts(
            @Valid @ModelAttribute ProductSearchCriteria criteria,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.searchProducts(criteria, pageable);
    }

    private Sort buildSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.unsorted();
        }

        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        if (property.isEmpty()) {
            return Sort.unsorted();
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            direction = Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.ASC);
        }
        return Sort.by(direction, property);
    }
}
