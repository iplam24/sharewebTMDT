// src/main/java/com/shop/controller/review/ReviewController.java
package com.shop.controller.review;

import com.shop.dto.review.ReviewCreateForm;
import com.shop.dto.review.ReviewRequestDTO;
import com.shop.dto.review.ReviewResponseDTO;
import com.shop.dto.review.ReviewUpdateDTO;
import com.shop.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponseDTO> create(@ModelAttribute ReviewCreateForm form) throws IOException {
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setProductId(form.getProductId());
        dto.setRating(form.getRating());
        dto.setComment(form.getComment());

        return ResponseEntity.ok(reviewService.createReview(dto, form.getImages()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> update(
            @PathVariable Long id,
            @RequestBody ReviewUpdateDTO dto
    ) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }


}