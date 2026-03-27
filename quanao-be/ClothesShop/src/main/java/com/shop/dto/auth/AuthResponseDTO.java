// src/main/java/com/shop/dto/auth/AuthResponseDTO.java
package com.shop.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String username;
    private String role;
    // Có thể thêm email nếu frontend cần
    // private String email;
    private String accessToken;

}