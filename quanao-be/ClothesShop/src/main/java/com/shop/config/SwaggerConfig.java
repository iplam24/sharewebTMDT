// src/main/java/com/shop/config/SwaggerConfig.java
package com.shop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth"; // Tên của scheme

        return new OpenAPI()
                // 1. Thêm nút "Authorize"
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 2. Định nghĩa scheme "bearerAuth"
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP) // Kiểu HTTP
                                                .scheme("bearer") // Scheme là "bearer"
                                                .bearerFormat("JWT") // Định dạng là JWT
                                )
                )
                // (Tùy chọn) Thêm thông tin chung cho API
                .info(new Info().title("Clothing Shop API").version("v1"));
    }
}