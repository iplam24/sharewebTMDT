package com.shop.repository.cart;

import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm theo Cart và Variant
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);
}