package com.shop.service.file;

import com.shop.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    // Cấu hình đường dẫn linh hoạt: mặc định vào thư mục 'storage/uploads' trong project
    public FileStorageService(@Value("${file.upload-dir:storage/uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File rỗng.");
            }

            // Kiểm tra định dạng (JPG, PNG, WebP)
            String contentType = file.getContentType();
            if (contentType == null || !List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
                throw new RuntimeException("Chỉ hỗ trợ định dạng: JPG, PNG, WebP");
            }

            // Kiểm tra kích thước (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("File quá lớn, tối đa 5MB");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Lưu file thất bại", e);
        }
    }

    // --- CÁC PHƯƠNG THỨC BỊ THIẾU CẦN BỔ SUNG ---

    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path filePath = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Xóa file thất bại: " + filename, e);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new ResourceNotFoundException("File không tồn tại hoặc không thể đọc: " + filename);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Đường dẫn file không hợp lệ: " + filename);
        }
    }

    public String getFileUrl(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/") // Khớp với mapping trong Controller của bạn
                .path(filename)
                .toUriString();
    }
}