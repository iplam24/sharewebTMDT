package com.shop.config;

import com.shop.entity.User;
import com.shop.enums.Role;
import com.shop.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitUserConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {

            // Nếu đã có admin rồi thì bỏ qua
            if (userRepository.existsByUsername("admin")) {
                return;
            }

            // ====== TẠO ADMIN ======
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@gmail.com")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);

            System.out.println("Đã khởi tạo tài khoản ADMIN mặc định!");
        };
    }
}
