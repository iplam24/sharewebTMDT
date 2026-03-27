// src/main/java/com/shop/config/ApplicationConfig.java
package com.shop.config;

import com.shop.exception.ResourceNotFoundException; // Import exception của bạn
import com.shop.repository.user.UserRepository; // Import repository của bạn
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Cung cấp Bean UserDetailsService.
     * Spring Security sẽ dùng Bean này để biết cách load thông tin User từ database.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
        // Bạn có thể dùng ResourceNotFoundException nếu muốn, nhưng UsernameNotFoundException là chuẩn của Spring Security
    }

    /**
     * Cung cấp Bean PasswordEncoder.
     * Spring Security sẽ dùng Bean này để mã hóa và so sánh mật khẩu.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cung cấp Bean AuthenticationManager.
     * Bean này được AuthService sử dụng để xác thực người dùng khi họ đăng nhập.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cung cấp Bean AuthenticationProvider.
     * Đây là "cầu nối" cho Spring Security biết rằng
     * hãy dùng UserDetailsService (ở trên) và PasswordEncoder (ở trên) để xác thực.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}