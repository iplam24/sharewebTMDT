// src/main/java/com/shop/service/review/ReviewServiceImpl.java
package com.shop.service.review;

import com.shop.dto.review.ReviewRequestDTO;
import com.shop.dto.review.ReviewResponseDTO;
import com.shop.dto.review.ReviewUpdateDTO;
import com.shop.entity.*;
import com.shop.enums.OrderStatus;
import com.shop.exception.ResourceNotFoundException;
import com.shop.repository.order.OrderRepository;
import com.shop.repository.product.ProductRepository;
import com.shop.repository.review.ReviewRepository;
import com.shop.service.file.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO dto, List<MultipartFile> images) throws IOException {
        User user = getCurrentUser();

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        boolean hasPurchased = orderRepository
                .existsByUserAndStatusAndItemsProductId(user, OrderStatus.DELIVERED, product.getId());

        if (!hasPurchased) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sau khi mua hàng thành công");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .createdAt(LocalDateTime.now())
                .images(new ArrayList<>())
                .build();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;
                String filename = fileStorageService.store(file);
                String url = fileStorageService.getFileUrl(filename);

                ReviewImage img = ReviewImage.builder()
                        .imageUrl(url)
                        .build();
                review.addImage(img); // CHỈ DÙNG METHOD NÀY
            }
        }

        Review saved = reviewRepository.save(review);
        return ReviewResponseDTO.from(saved);
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewUpdateDTO dto) {
        User user = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không thể sửa đánh giá của người khác");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        if (dto.getImageUrls() != null) {
            // XÓA FILE CŨ TRƯỚC
            review.getImages().forEach(img -> {
                String filename = extractFilenameFromUrl(img.getImageUrl());
                fileStorageService.deleteFile(filename);
            });

            review.getImages().clear();

            dto.getImageUrls().forEach(url -> {
                ReviewImage img = ReviewImage.builder()
                        .imageUrl(url)
                        .build();
                review.addImage(img);
            });
        }

        Review saved = reviewRepository.save(review);
        return ReviewResponseDTO.from(saved);
    }

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        User user = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không thể xoá đánh giá của người khác");
        }

        // XÓA FILE TRƯỚC KHI XÓA ENTITY
        review.getImages().forEach(img -> {
            String filename = extractFilenameFromUrl(img.getImageUrl());
            fileStorageService.deleteFile(filename);
        });

        reviewRepository.delete(review);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(ReviewResponseDTO::from)
                .toList();
    }
}