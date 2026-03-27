package com.shop.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequestDTO {
    @NotNull(message = "Phải chọn phân loại sản phẩm (Màu/Size)")
    private Long variantId;

    @NotNull
    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    private int quantity;
}