package com.shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class này dùng để đọc các cấu hình VNPay từ application.properties/yml.
 * Prefix "vnpay" sẽ khớp với các key như vnpay.pay-url, vnpay.tmn-code, vnpay.secret-key, vnpay.api-url.
 */
@Component
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VNPayProperties {
    private String payUrl;        // vnpay.pay-url
    private String returnUrl;     // vnpay.return-url (URL callback của ứng dụng sau khi thanh toán)
    private String tmnCode;       // vnpay.tmn-code (Mã website của bạn tại VNPAY)
    private String secretKey;     // vnpay.secret-key (Key bảo mật do VNPAY cung cấp)
    private String apiUrl;        // vnpay.api-url (URL API truy vấn của VNPAY)
    private String version = "2.1.0"; // vnpay.version (Phiên bản API, thường không đổi)
    private String command = "pay";   // vnpay.command (Mã API request, thường là "pay")
    private String currencyCode = "VND"; // vnpay.currency-code (Mã tiền tệ, mặc định "VND")
    private String locale = "vn";     // vnpay.locale (Ngôn ngữ hiển thị trên cổng VNPAY, "vn" hoặc "en")


}