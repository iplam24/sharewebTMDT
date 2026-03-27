package com.shop.controller.auth;

import com.shop.entity.User;
import com.shop.enums.Role;
import com.shop.service.auth.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
// Đảm bảo chỉ những người có quyền ADMIN mới truy cập được các endpoint này
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;


    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<User> usersPage = adminUserService.getAllUsers(page, size);
        return ResponseEntity.ok(usersPage);
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long userId,
            // Spring Boot sẽ tự động chuyển đổi chuỗi từ Request Param sang Enum Role
            @RequestParam Role role) {

        User updatedUser = adminUserService.updateUserRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
}