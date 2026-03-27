package com.shop.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSaleStatDTO {
    private Long productId;
    private String productName;
    private Long totalSold;
}
