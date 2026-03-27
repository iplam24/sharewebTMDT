package com.shop.mapper.category;

import com.shop.dto.category.CategoryDTO;
import com.shop.dto.category.CategoryRequestDTO;
import com.shop.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO toDTO(Category category);
    List<CategoryDTO> toDTOList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", expression = "java(com.shop.utils.SlugUtils.toSlug(requestDTO.getName()))") // Sử dụng hàm tạo slug
    @Mapping(target = "products", ignore = true) // Bỏ qua quan hệ
    Category toEntity(CategoryRequestDTO requestDTO);
}