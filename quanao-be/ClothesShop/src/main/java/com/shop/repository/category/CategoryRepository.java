package com.shop.repository.category;

import com.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Tìm danh mục theo tên (để kiểm tra trùng lặp khi tạo)
    Optional<Category> findByNameIgnoreCase(String name);

    // Lấy tất cả danh mục chưa bị xóa mềm
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    List<Category> findAllActive();
}
