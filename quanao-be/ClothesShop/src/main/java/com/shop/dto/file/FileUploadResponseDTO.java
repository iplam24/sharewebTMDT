// src/main/java/com/shop/dto/FileUploadResponseDTO.java
package com.shop.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List; // <-- THÊM IMPORT NÀY

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponseDTO {
    // Sửa từ "private String url;" thành "private List<String> urls;"
    private List<String> urls;
}