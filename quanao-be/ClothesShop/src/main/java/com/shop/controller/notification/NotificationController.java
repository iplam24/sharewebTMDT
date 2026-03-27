package com.shop.controller.notification;

import com.shop.dto.response.MessageResponseDTO;
import com.shop.entity.Notification;
import com.shop.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String content = payload.get("content");

        if (title == null || content == null) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Tiêu đề và nội dung không được để trống."));
        }

        notificationService.createAndSendNotification(title, content);

        return ResponseEntity.ok(new MessageResponseDTO("Thông báo đã được gửi thành công đến tất cả người dùng."));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
}
