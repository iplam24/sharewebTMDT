package com.shop.config;

import com.shop.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // BẬT CORS: dùng cấu hình trong WebConfig
                .cors(Customizer.withDefaults())
                // TẮT CSRF cho API
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Cho phép preflight OPTIONS (rất quan trọng)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // === 1. PUBLIC (permitAll) ===
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/me").permitAll()
                        .requestMatchers("/api/vnpay/payment-return").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/api/vnpay/**",
                                "/api/file/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/discounts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/discounts/**").permitAll()

                        // === 2. AUTHENTICATED (YÊU CẦU ĐĂNG NHẬP) ===
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers("/api/favorites/**").authenticated()
                        .requestMatchers("/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/orders/{orderId}/confirm-delivery").authenticated()

                        // === 3. ADMIN ===
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Cho phép tất cả API dưới /api/admin

                        // Các request còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
