package com.shop.service.review;

import com.shop.dto.review.ReviewRequestDTO;
import com.shop.dto.review.ReviewResponseDTO;
import com.shop.dto.review.ReviewUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReviewService {

    ReviewResponseDTO createReview(ReviewRequestDTO dto, List<MultipartFile> images) throws IOException;

    ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO dto);

    void deleteReview(Long reviewId);

    List<ReviewResponseDTO> getReviewsByProduct(Long productId);
}
