package com.shop.utils;

import com.shop.config.VNPayProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class VNPayUtils {

    private final VNPayProperties vnPayProperties;

    public String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("Error getting IP address: {}", e.getMessage());
            ipAdress = "UNKNOWN";
        }
        return ipAdress;
    }

    public String hmacSHA512(final String data) {
        try {
            if (vnPayProperties.getSecretKey() == null || data == null) {
                throw new IllegalArgumentException("Secret key or data cannot be null for HMACSHA512.");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = vnPayProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKeySpec = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException ex) {
            log.error("Error generating HMACSHA512: {}", ex.getMessage());
            throw new RuntimeException("Failed to generate HMACSHA512 hash.", ex);
        }
    }

    /**
     * Build chuỗi data để ký (request + return + IPN đều dùng):
     * - Chỉ lấy các param bắt đầu bằng "vnp_"
     * - Bỏ vnp_SecureHash, vnp_SecureHashType nếu có (đã remove từ ngoài service)
     * - Sort key tăng dần
     * - Ghép key=value (URI-encode value) nối bằng '&'
     */
    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    // Key: không encode
                    // Value: phải encode UTF-8
                    sb.append(fieldName)
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    if (itr.hasNext()) {
                        sb.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("UTF-8 not supported", e);
                }
            }
        }
        return sb.toString();
    }


    public String createVNPayPaymentUrl(Long orderId,
                                        long amount,
                                        String orderInfo,
                                        HttpServletRequest request)
            throws UnsupportedEncodingException {

        String vnp_TxnRef = getRandomNumber(8); // Mã giao dịch VNPay
        String vnp_IpAddr = getIpAddress(request);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayProperties.getVersion());
        vnp_Params.put("vnp_Command", vnPayProperties.getCommand());
        vnp_Params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", vnPayProperties.getCurrencyCode());
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

        vnp_Params.put("vnp_OrderInfo", orderInfo + " - OrderID: " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", vnPayProperties.getLocale());


        vnp_Params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl() + "?orderId=" + orderId);

        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        String hashData = hashAllFields(vnp_Params);
        String vnp_SecureHash = hmacSHA512(hashData);

        StringBuilder query = new StringBuilder();
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null) {
                // SỬA ĐỔI QUAN TRỌNG: Đảm bảo sử dụng UTF-8 khi tạo query string
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append("=");
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                query.append("&");
            }
        }

        query.append("vnp_SecureHash=").append(vnp_SecureHash);

        return vnPayProperties.getPayUrl() + "?" + query;
    }

}