package com.company.shop.module.product.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.product.dto.ProductCreateDTO;
import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Products", description = "Administracyjne zarządzanie produktami.")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobranie produktu po ID (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt znaleziony."),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony."),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień.")
    })
    public ProductResponseDTO getProductById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Utworzenie produktu (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produkt utworzony poprawnie."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane żądania."),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień.")
    })
    public ProductResponseDTO createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        return productService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizacja produktu (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt zaktualizowany poprawnie."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane żądania."),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień."),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony.")
    })
    public ProductResponseDTO updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductCreateDTO dto) {
        return productService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Usunięcie produktu (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produkt usunięty."),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień."),
            @ApiResponse(responseCode = "404", description = "Produkt nie został znaleziony.")
    })
    public void deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
    }
}
