package com.shop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Tên danh mục (QUẦN, ÁO...)

    private String slug; // Slug (Dùng cho URL)

    // Quan hệ 1-Nhiều với Product
    @OneToMany(mappedBy = "category")
    private List<Product> products;

    // THÊM CÁC TRƯỜNG NÀY (nếu chưa có)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // `updatable = false` cho createdAt

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Trường đánh dấu xóa mềm
}
