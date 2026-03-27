package com.shop.service.cart;

import com.shop.dto.cart.AddToCartRequestDTO;
import com.shop.dto.cart.CartResponseDTO;

public interface CartService {


    CartResponseDTO getCartForCurrentUser();


    CartResponseDTO addProductToCart(AddToCartRequestDTO requestDTO);


    CartResponseDTO removeProductFromCart(Long cartItemId);


    CartResponseDTO updateProductQuantity(Long cartItemId, int quantity);
}