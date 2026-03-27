package com.shop.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

//    @NotBlank(message = "Phân loại không được để trống")
//    private String category;

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    // Danh sách URL ảnh
    private List<String> imageUrls;

    // Danh sách biến thể (Màu/Size/Stock)
    private List<ProductVariantDTO> variants;
}