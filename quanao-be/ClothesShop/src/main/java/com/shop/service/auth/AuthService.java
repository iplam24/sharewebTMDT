// src/main/java/com/shop/service/auth/AuthService.java
package com.shop.service.auth;

import com.shop.dto.auth.AuthResponseDTO;
import com.shop.dto.auth.LoginRequestDTO;
import com.shop.dto.auth.RegisterRequestDTO;
import com.shop.entity.Cart;
import com.shop.enums.Role;
import com.shop.entity.User;
import com.shop.repository.user.UserRepository;
import com.shop.security.JwtService;
import com.shop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Đảm bảo import này

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * Kiểm tra email tồn tại (Hỗ trợ API Forgot Password)
     */
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Xử lý logic Quên Mật Khẩu (MỚI THÊM)
     */
    public void sendForgotPasswordEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));


        String otp = String.format("%06d", new Random().nextInt(999999));

        // 2. Lưu OTP vào DB (Hết hạn sau 15 phút)
        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // 3. Gửi email
        String subject = "Mã xác nhận khôi phục mật khẩu - CLOTHES VNUA";
        String htmlContent = "<h3>Xin chào " + user.getUsername() + ",</h3>"
                + "<p>Bạn đã yêu cầu đặt lại mật khẩu. Mã xác nhận của bạn là:</p>"
                + "<h2 style='color: #d0021b;'>" + otp + "</h2>"
                + "<p>Mã này sẽ hết hạn sau 15 phút. <b>Nếu bạn không yêu cầu, hãy bỏ qua email này.</b></p>";

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    /**
     * BƯỚC 2: Xác thực OTP và Đổi mật khẩu
     */
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        // 1. Kiểm tra OTP có khớp không
        if (user.getResetToken() == null || !user.getResetToken().equals(otp)) {
            throw new RuntimeException("Mã xác nhận không chính xác!");
        }

        // 2. Kiểm tra OTP còn hạn không
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn!");
        }

        // 3. Đổi mật khẩu
        user.setPassword(passwordEncoder.encode(newPassword));

        // 4. Xóa OTP sau khi dùng xong
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }
    /**
     * Xử lý logic Đăng ký
     * * @return User Entity đã được lưu (đảm bảo transaction đã commit)
     */
    @Transactional // Đảm bảo toàn bộ logic đăng ký là 1 transaction
    public User register(RegisterRequestDTO request) { // <--- THAY ĐỔI RETURN TYPE THÀNH USER
        // 1. Kiểm tra username/email tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Lỗi: Username đã được sử dụng!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Lỗi: Email đã được sử dụng!");
        }

        // 2. Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        // 3. Tạo một Cart mới
        Cart newCart = new Cart();
        newCart.setUser(user);
        user.setCart(newCart);

        // 4. Lưu user vào DB
        User savedUser = userRepository.save(user);

        // 5. Tạo JWT token (KHÔNG CẦN TẠO Ở SERVICE NỮA)

        // 6. Trả về Entity đã lưu
        return savedUser; // <--- TRẢ VỀ ENTITY
    }

    /**
     * Xử lý logic Đăng nhập
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(jwtToken)
                .build();
    }
}
