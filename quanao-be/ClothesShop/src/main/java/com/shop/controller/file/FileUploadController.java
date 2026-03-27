// src/main/java/com/shop/controller/FileUploadController.java
package com.shop.controller.file;

import com.shop.dto.file.FileUploadResponseDTO;
import com.shop.service.file.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList; // <-- THÊM IMPORT
import java.util.List; // <-- THÊM IMPORT

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload một hoặc nhiều file ảnh",
            description = "API này nhận một mảng file ảnh và trả về một danh sách URL.")
    public ResponseEntity<FileUploadResponseDTO> uploadMultipleFiles( // Đổi tên phương thức
                                                                      @RequestParam("files") MultipartFile[] files
    ) {

        List<String> urls = new ArrayList<>();

        // Lặp qua từng file mà người dùng upload
        for (MultipartFile file : files) {
            // 1. Lưu file và lấy tên file
            String filename = fileStorageService.store(file);

            // 2. Tạo URL đầy đủ
            String url = fileStorageService.getFileUrl(filename);

            // 3. Thêm URL vào danh sách
            urls.add(url);
        }

        // 4. Trả về DTO chứa danh sách URL
        return ResponseEntity.ok(new FileUploadResponseDTO(urls));
    }
}