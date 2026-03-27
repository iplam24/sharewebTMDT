package com.shop.repository.cart;

import com.shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng bằng ID của User
    Optional<Cart> findByUserId(Long userId);
}