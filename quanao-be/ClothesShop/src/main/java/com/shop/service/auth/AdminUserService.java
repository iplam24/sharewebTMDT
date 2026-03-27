package com.shop.service.auth;

import com.shop.entity.User;
import com.shop.enums.Role;
import com.shop.exception.ResourceNotFoundException;
// Đã thay đổi import để phù hợp với cấu trúc repository của bạn
import com.shop.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;


    public Page<User> getAllUsers(int page, int size) {
        // Sort theo ID giảm dần (descending)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return userRepository.findAll(pageable);
    }


    @Transactional
    public User updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Logic kiểm tra bảo mật (nên được xử lý ở đây hoặc Controller/Security)
        // Ví dụ: Không cho phép Admin hạ cấp chính mình.

        if (user.getRole().equals(newRole)) {
            // Không cần cập nhật nếu Role đã giống
            return user;
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }
}