package com.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO<T> {
    private List<T> content; // Nội dung của trang hiện tại
    private int totalPages; // Tổng số trang
    private long totalElements; // Tổng số phần tử
    private int currentPage; // Trang hiện tại (0-indexed hoặc 1-indexed)
    private int pageSize; // Kích thước trang
}