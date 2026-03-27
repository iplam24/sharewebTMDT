package com.shop.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;

    private String category;
    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    private List<String> imageUrls;
    private List<ProductVariantDTO> variants;


    // Tự động tính tổng tồn kho để hiển thị ra bên ngoài cho tiện
    public int getTotalStock() {
        if (variants == null) return 0;
        return variants.stream().mapToInt(ProductVariantDTO::getStockQuantity).sum();
    }
}