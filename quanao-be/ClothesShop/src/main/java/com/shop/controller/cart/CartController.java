package com.shop.controller.cart;

import com.shop.dto.cart.AddToCartRequestDTO;
import com.shop.dto.cart.CartResponseDTO;
import com.shop.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart") // Tất cả API giỏ hàng sẽ bắt đầu bằng /api/cart
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        return ResponseEntity.ok(cartService.getCartForCurrentUser());
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @Valid @RequestBody AddToCartRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(cartService.addProductToCart(requestDTO));
    }


    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartResponseDTO> removeItemFromCart(
            @PathVariable Long cartItemId
    ) {
        return ResponseEntity.ok(cartService.removeProductFromCart(cartItemId));
    }


    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.updateProductQuantity(cartItemId, quantity));
    }
}