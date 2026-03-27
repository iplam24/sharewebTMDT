package com.shop.dto.order;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;

    // Đã có trong OrderItem Entity
    private String color;
    private String size;

    private int quantity;
    private BigDecimal priceAtPurchase;

    // CẦN THIẾT: Trường để chứa URL ảnh
    private String imageUrl;
}