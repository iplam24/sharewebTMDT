package com.shop.dto.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.shop.entity.Review;
import com.shop.entity.ReviewImage;

@Data
public class ReviewResponseDTO {

    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> imageUrls;

    public static ReviewResponseDTO from(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        dto.setImageUrls(
                review.getImages()
                        .stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList())
        );

        return dto;
    }
}
