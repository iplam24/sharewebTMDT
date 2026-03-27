package com.shop.controller.category;

import com.shop.dto.category.CategoryDTO;
import com.shop.dto.category.CategoryRequestDTO;
import com.shop.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // Thay đổi root path thành /api
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // --- API CÔNG KHAI: Lấy danh sách tất cả danh mục ---
    // Endpoint: GET /api/categories
    // Quyền: permitAll (được phép truy cập bởi tất cả mọi người)
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllPublicCategories() {
        return ResponseEntity.ok(categoryService.findAllPublicCategories());
    }

    // --- API ADMIN: Quản lý danh mục (CHỈ ADMIN MỚI ĐƯỢC GỌI) ---
    // Endpoint: /api/admin/categories
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getAllAdminCategories() {
        // Có thể gọi lại findAllPublicCategories() nếu logic admin không khác biệt nhiều
        return ResponseEntity.ok(categoryService.findAllCategories());
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryRequestDTO request) {
        CategoryDTO newCategory = categoryService.createCategory(request);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO request) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.findCategoryById(id);
        return ResponseEntity.ok(category);
    }
}