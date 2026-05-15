package com.company.shop.module.cart.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.shop.module.cart.dto.AddToCartRequestDTO;
import com.company.shop.module.cart.dto.CartResponseDTO;
import com.company.shop.module.cart.dto.UpdateCartItemRequestDTO;
import com.company.shop.module.cart.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/me/cart")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Cart", description = "Operacje na koszyku aktualnie zalogowanego użytkownika.")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Pobranie koszyka użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Koszyk pobrany poprawnie."),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji.")
    })
    public ResponseEntity<CartResponseDTO> getCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    @Operation(summary = "Dodanie produktu do koszyka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt dodany do koszyka."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane żądania."),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji.")
    })
    public ResponseEntity<CartResponseDTO> addCartItem(@Valid @RequestBody AddToCartRequestDTO request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Aktualizacja ilości produktu w koszyku")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ilość produktu zaktualizowana."),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane żądania."),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji.")
    })
    public ResponseEntity<CartResponseDTO> updateCartItemQuantity(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(productId, request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Usunięcie produktu z koszyka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produkt usunięty z koszyka."),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji.")
    })
    public ResponseEntity<CartResponseDTO> removeCartItem(@PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(productId));
    }

    @DeleteMapping
    @Operation(summary = "Wyczyszczenie koszyka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Koszyk wyczyszczony."),
            @ApiResponse(responseCode = "401", description = "Brak autoryzacji.")
    })
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
