package com.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
    private Date timestamp;
    private String message;
    private String details;

    // Constructor nhanh gọn
    public ErrorResponseDTO(String message) {
        this.timestamp = new Date();
        this.message = message;
        this.details = null;
    }
}