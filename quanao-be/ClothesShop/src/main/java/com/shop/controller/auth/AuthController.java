// src/main/java/com/shop/controller/auth/AuthController.java
package com.shop.controller.auth;

import com.shop.dto.auth.*;
import com.shop.dto.response.MessageResponseDTO;
import com.shop.entity.User;
import com.shop.security.JwtService;
import com.shop.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService; // cần để tạo token
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request,
            HttpServletResponse response) {

        // 1. Đăng ký và lấy Entity User đã lưu
        User newUser = authService.register(request);

        // 2. Tạo AuthResponseDTO từ Entity User
        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .username(newUser.getUsername())
                .role(newUser.getRole().name())
                .build();

        // 3. Tạo token trực tiếp từ Entity (Entity User giả định là implements UserDetails)
        String jwtToken = jwtService.generateToken(newUser);
        setJwtCookie(response, jwtToken,false);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {

        AuthResponseDTO authResponse = authService.login(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        setJwtCookie(response, jwtToken, request.isRememberMe());
        return ResponseEntity.ok(authResponse);
    }

    // === API: QUÊN MẬT KHẨU (KHẮC PHỤC LỖI 404) ===
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Email không được để trống."));
        }
        // Gọi Service để xử lý tìm kiếm và gửi OTP
        // Service ném RuntimeException nếu email không tồn tại
        authService.sendForgotPasswordEmail(email);
        return ResponseEntity.ok(new MessageResponseDTO("Mã xác nhận đã được gửi đến email của bạn."));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Thông tin không được để trống."));
        }

        // Gọi Service để xử lý xác thực OTP và đổi mật khẩu
        authService.resetPassword(email, otp, newPassword);

        return ResponseEntity.ok(new MessageResponseDTO("Đặt lại mật khẩu thành công."));
    }


    private void setJwtCookie(HttpServletResponse response, String token, boolean rememberMe) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);   // đúng cho http://localhost
        cookie.setPath("/");
        cookie.setMaxAge(rememberMe ? 7 * 24 * 60 * 60 : 24 * 60 * 60);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);           // thêm cho giống lúc set
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        return ResponseEntity.ok(new MessageResponseDTO("Đăng xuất thành công"));
    }

}