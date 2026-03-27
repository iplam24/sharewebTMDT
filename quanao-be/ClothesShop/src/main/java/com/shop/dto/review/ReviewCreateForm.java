// src/main/java/com/shop/dto/review/ReviewCreateForm.java
package com.shop.dto.review;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ReviewCreateForm {
    private Long productId;
    private int rating;
    private String comment;
    private List<MultipartFile> images;
}