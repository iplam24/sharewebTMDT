// src/main/java/com/shop/entity/ProductImage.java
package com.shop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // Đường dẫn URL của ảnh

    // Đây là "phía nhiều" (Many-to-One) của mối quan hệ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // Tên cột khóa ngoại
    @JsonIgnore // Quan trọng: Tránh lỗi lặp vô hạn khi serialize JSON
    private Product product;
}