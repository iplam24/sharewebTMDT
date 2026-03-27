package com.shop.dto.review;

import lombok.Data;

import java.util.List;

@Data
public class ReviewUpdateDTO {
    private int rating;
    private String comment;
    // gửi lại danh sách URL mới (nếu FE cho sửa ảnh kiểu chọn lại)
    private List<String> imageUrls;
}
