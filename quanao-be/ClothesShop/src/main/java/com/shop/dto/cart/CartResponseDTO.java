package com.shop.dto.cart;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {
    private Long id; // ID của Cart
    private List<CartItemDTO> items;
    private BigDecimal totalPrice; // Tổng tiền
}