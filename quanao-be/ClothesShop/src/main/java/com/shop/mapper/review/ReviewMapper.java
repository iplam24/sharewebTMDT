package com.shop.mapper.review;

import com.shop.dto.review.ReviewResponseDTO;
import com.shop.entity.Review;
import com.shop.entity.ReviewImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "imageUrls", source = "images", qualifiedByName = "toImageUrls")
    ReviewResponseDTO toDTO(Review review);

    List<ReviewResponseDTO> toDTOList(List<Review> reviews);

    @Named("toImageUrls")
    default List<String> toImageUrls(List<ReviewImage> images) {
        if (images == null || images.isEmpty()) return Collections.emptyList();
        return images.stream().map(ReviewImage::getImageUrl).collect(Collectors.toList());
    }
}
