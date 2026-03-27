// src/main/java/com/shop/service/category/CategoryServiceImpl.java
package com.shop.service.category;

import com.shop.dto.category.CategoryDTO;
import com.shop.dto.category.CategoryRequestDTO;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.category.CategoryMapper;
import com.shop.entity.Category;
import com.shop.repository.category.CategoryRepository;
import com.shop.utils.SlugUtils; // <-- IMPORT SLUG UTILS
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> findAllCategories() {
        // Lấy tất cả danh mục chưa bị xóa mềm
        return categoryMapper.toDTOList(categoryRepository.findAllActive());
    }

    @Override
    public CategoryDTO createCategory(CategoryRequestDTO request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(SlugUtils.toSlug(request.getName())); // <-- TẠO VÀ SET SLUG
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = findCategoryEntityById(id);
        category.setName(request.getName());
        category.setSlug(SlugUtils.toSlug(request.getName())); // <-- CẬP NHẬT SLUG KHI UPDATE
        category.setUpdatedAt(LocalDateTime.now());
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Thực hiện xóa mềm
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO findCategoryById(Long id) {
        Category category = findCategoryEntityById(id);
        return categoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findCategoryEntityById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Kiểm tra nếu danh mục đã bị xóa mềm
        if (category.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> findAllPublicCategories() {
        // Lấy tất cả danh mục chưa bị xóa mềm
        return categoryMapper.toDTOList(categoryRepository.findAllActive());
    }
}
