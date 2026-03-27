// src/main/java/com/shop/repository/product/ProductImageRepository.java
package com.shop.repository.product;

import com.shop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}