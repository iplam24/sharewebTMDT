package com.shop.dto.product;

import lombok.Data;

@Data
public class ProductVariantDTO {
    private Long id;
    private String color;
    private String size;
    private int stockQuantity;
}