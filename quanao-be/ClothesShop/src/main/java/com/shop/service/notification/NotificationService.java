package com.shop.service.notification;

import com.shop.entity.Notification;
import com.shop.repository.notification.NotificationRepository;
import com.shop.repository.user.UserRepository;
import com.shop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public Notification createAndSendNotification(String title, String content) {
        // 1. Lưu thông báo vào DB
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .build();
        Notification savedNotification = notificationRepository.save(notification);

        // 2. Lấy danh sách email
        List<String> allEmails = userRepository.findAllEmails();

        // 3. Gửi email bất đồng bộ (Async)
        if (!allEmails.isEmpty()) {
            emailService.sendBulkEmailAsync(allEmails, title, content);
        }

        return savedNotification;
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
