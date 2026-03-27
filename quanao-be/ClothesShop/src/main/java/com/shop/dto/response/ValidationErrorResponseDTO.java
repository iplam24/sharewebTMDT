package com.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponseDTO {

    private Date timestamp;
    private String message;           // Ví dụ: "Dữ liệu không hợp lệ"
    private String path;              // URI request (/api/products)
    private Map<String, String> errors; // field -> message

}
