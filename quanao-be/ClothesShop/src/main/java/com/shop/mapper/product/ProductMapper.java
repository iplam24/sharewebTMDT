package com.shop.mapper.product;

import com.shop.dto.product.ProductDTO;
import com.shop.dto.product.ProductRequestDTO;
import com.shop.dto.product.ProductVariantDTO;
import com.shop.entity.Product;
import com.shop.entity.ProductImage;
import com.shop.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // === TO DTO (RESPONSE) ===
    // 1. Ánh xạ 'category' Entity sang 'category' String (lấy tên)
    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "imagesToImageUrls")
    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTOList(List<Product> products);

    // === TO ENTITY (CREATE) ===
    // 1. Dùng target="category" (đúng tên field Entity) thay vì category.name
    // 2. Bỏ qua các trường Entity không có trong DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "isDeleted", ignore = true) // Cờ xóa mềm (Lombok Builder)
    @Mapping(target = "category", ignore = true) // Category sẽ được set ID trong Service
    Product toEntity(ProductRequestDTO requestDTO);

    // === TO ENTITY (UPDATE) ===
    // Dùng target="deleted" cho Setter
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "deleted", ignore = true)   // Cờ xóa mềm (Lombok Setter)
    @Mapping(target = "category", ignore = true) // Category sẽ được set ID trong Service
    void updateEntityFromDTO(ProductRequestDTO requestDTO, @MappingTarget Product product);

    @Named("imagesToImageUrls")
    default List<String> imagesToImageUrls(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return Collections.emptyList();
        return images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
    }

    ProductVariantDTO toVariantDTO(ProductVariant variant);
    ProductVariant toVariantEntity(ProductVariantDTO dto);
}