package com.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
// Cấu hình Xóa Mềm (Soft Delete)
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    //private String category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // Khóa ngoại trong bảng products
    private Category category;


    @Column(nullable = false)
    private boolean isDeleted = false; // Cờ xóa mềm

    // Quan hệ 1-Nhiều với Variant (Size/Màu)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    // Quan hệ 1-Nhiều với Image
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();



    public void addVariant(String color, String size, int stock) {
        ProductVariant variant = ProductVariant.builder()
                .color(color)
                .size(size)
                .stockQuantity(stock)
                .product(this)
                .build();
        this.variants.add(variant);
    }

    public void addImage(String url) {
        ProductImage image = ProductImage.builder()
                .imageUrl(url)
                .product(this)
                .build();
        this.images.add(image);
    }
}