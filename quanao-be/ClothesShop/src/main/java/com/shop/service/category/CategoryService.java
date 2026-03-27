package com.shop.service.category;

import com.shop.dto.category.CategoryDTO;
import com.shop.dto.category.CategoryRequestDTO;
import com.shop.entity.Category;

import java.util.List;

public interface CategoryService {
    // --- Phương thức cho Admin (giữ nguyên) ---
    List<CategoryDTO> findAllCategories(); // Có thể đổi tên thành findAllAdminCategories nếu muốn rõ ràng hơn
    CategoryDTO createCategory(CategoryRequestDTO request);
    CategoryDTO updateCategory(Long id, CategoryRequestDTO request);
    void deleteCategory(Long id);
    CategoryDTO findCategoryById(Long id);
    Category findCategoryEntityById(Long id); // Phương thức nội bộ để lấy entity

    // --- PHƯƠNG THỨC MỚI CHO PUBLIC API ---
    List<CategoryDTO> findAllPublicCategories();
}